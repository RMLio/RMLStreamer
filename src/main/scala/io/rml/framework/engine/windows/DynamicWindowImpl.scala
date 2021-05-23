package io.rml.framework.engine.windows

import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.util.Collector

import scala.collection.JavaConversions.iterableAsScalaIterable

/**
  * DynamicWindow receives tuples already keyed based on their joining attributes, therefore
  * we could just execute a cross join inside the window accordingly to get the resulting list of JoinedItems.
  *
  * @param updateCycle fixed cycle where the window length will be updated according to the calculations
  * @tparam T subtypes of Iterable[Item]
  * @tparam U subtypes of Iterable[Item]
  */
class DynamicWindowImpl[T <: Iterable[Item], U <: Iterable[Item]](val epsilon: Double = 1.5,
                                                                  val defaultBucketSize: Long = 1000L,
                                                                  val maxDuration: Long = 5000L,
                                                                  val minDuration: Long = 50L,
                                                                  val updateCycle: Time = Time.seconds(2))
  extends KeyedCoProcessFunction[String, T, U, Iterable[JoinedItem]] {


  //Holds the list state of incoming child tuples that are still alive in the window
  lazy val childListState: ListState[Iterable[Item]] = getRuntimeContext.getListState(
    new ListStateDescriptor[Iterable[Item]]("VC_TWJoinChild", classOf[Iterable[Item]])
  )

  //Holds the list state of incoming parent tuples that are still alive in the window
  lazy val parentListState: ListState[Iterable[Item]] = getRuntimeContext.getListState(
    new ListStateDescriptor[Iterable[Item]]("VC_TWJoinParent", classOf[Iterable[Item]])
  )


  //Pseudo max limit of the hash buckets in the child map state
  lazy val pseudoChildLimit: ValueState[Long] = getRuntimeContext.getState(
    new ValueStateDescriptor[Long]("childLimit", classOf[Long])
  )

  //Pseudo max limit of the hash buckets in the parent map state
  lazy val pseudoParentLimit: ValueState[Long] = getRuntimeContext.getState(
    new ValueStateDescriptor[Long]("parentLimit", classOf[Long])
  )


  //Dynamic periodic update cycle which changes based on the velocity of the tuples
  lazy val dynamicUpdateCycle: ValueState[Time] = getRuntimeContext.getState(
    new ValueStateDescriptor[Time]("dynamicUpdateCycle", classOf[Time])
  )

  //Boolean flag to check if the callback has been fired.
  lazy val isCallBackFired: ValueState[Boolean] = getRuntimeContext.getState(
    new ValueStateDescriptor[Boolean]("isCallBackFired", classOf[Boolean])
  )


  private def initializeIfNotSet(): Unit = {
    if (isCallBackFired.value() == null) {
      isCallBackFired.update(false)
    }
    if (dynamicUpdateCycle.value() == null) {
      dynamicUpdateCycle.update(updateCycle)
    }
    if (pseudoParentLimit.value() == null) {
      pseudoParentLimit.update(defaultBucketSize)
    }
    if (pseudoChildLimit.value() == null) {
      pseudoChildLimit.update(defaultBucketSize)
    }
  }

  override def processElement1(child: T, context: KeyedCoProcessFunction[String, T, U, Iterable[JoinedItem]]#Context, collector: Collector[Iterable[JoinedItem]]): Unit = {
    initializeIfNotSet()
    val joinedItems = crossJoin(child, isChild = true, childListState, parentListState)

    joinedItems.foreach(collector.collect)
    fireWindowUpdateCallback(context)
  }

  override def processElement2(parent: U, context: KeyedCoProcessFunction[String, T, U, Iterable[JoinedItem]]#Context, collector: Collector[Iterable[JoinedItem]]): Unit = {
    initializeIfNotSet()
    val joinedItems = crossJoin(parent, isChild = false, parentListState, childListState)
    joinedItems.foreach(collector.collect)
    fireWindowUpdateCallback(context)
  }

  //TODO: Move this join function out of this class, such that the window can be provided with a
  //higher order function to be executed on the incoming elements.
  private def crossJoin(elementIter: Iterable[Item], isChild: Boolean, thisListState: ListState[Iterable[Item]], thatListState: ListState[Iterable[Item]]):
  Iterable[Iterable[JoinedItem]] = {
    thisListState.add(elementIter)

    thatListState.get()
      .map(thatElementIter =>
        thatElementIter.flatMap(item1 =>
          elementIter.map(item2 =>
            if (isChild)
              JoinedItem(item2, item1)
            else
              JoinedItem(item1, item2)
          )
        )
      )


  }

  /**
    * Fires the onTimer(..) method with a delay based on the dynamic update cycle.
    *
    * @param context
    */
  private def fireWindowUpdateCallback(context: KeyedCoProcessFunction[String, T, U, Iterable[JoinedItem]]#Context): Unit = {

    if (!isCallBackFired.value()) {
      context.timerService.registerEventTimeTimer(context.timestamp() + dynamicUpdateCycle.value().toMilliseconds)
    }
    isCallBackFired.update(true)
  }

  /**
    * Cyclic update of window size by dynamically adjusting the size according to the velocity of the
    * incoming tuples.
    *
    * @param timestamp timestamp of the callback based on either event time (watermark has passed) or processing time (system wall clock)
    * @param ctx
    * @param out
    */
  override def onTimer(timestamp: Long, ctx: KeyedCoProcessFunction[String, T, U, Iterable[JoinedItem]]#OnTimerContext, out: Collector[Iterable[JoinedItem]]): Unit = {

    val (join_entropy, childRatio, parentRatio) = calculateJoinCost()

    //Adjust pseudo limit based on the calculated ratios
    pseudoChildLimit.update((pseudoChildLimit.value() * childRatio).toLong)
    pseudoParentLimit.update((pseudoParentLimit.value() * parentRatio).toLong)

    val currentDynamicUpdateCycle = dynamicUpdateCycle.value().toMilliseconds
    val duration = if (join_entropy > epsilon && join_entropy > 0)
      currentDynamicUpdateCycle / 2 else
      currentDynamicUpdateCycle * 2

    dynamicUpdateCycle.update(Time.milliseconds(keepUpdateCycleWithinRange(duration)))

    cleanUpWindow()
  }

  private def keepUpdateCycleWithinRange(duration: Long): Long = {
    if (duration < minDuration) minDuration else if (duration > maxDuration) maxDuration else duration
  }

  private def cleanUpWindow(): Unit = {
    childListState.clear()
    pseudoChildLimit.clear()
    parentListState.clear()
    pseudoParentLimit.clear()
    isCallBackFired.clear()
    dynamicUpdateCycle.clear()

  }


  /**
    * Calculates the join entropy based on the paper VC-TWJoin: A Stream Join Algorithm Based on Variable Update Cycle Time Window
    *
    * @return
    */
  private def calculateJoinCost(): (Double, Double, Double) = {


    val childElementsProcessed: Double = childListState.get().size
    val parentElementsProcessed: Double = parentListState.get().size
    val childStreamN: Double = pseudoChildLimit.value()
    val parentStreamN: Double = pseudoParentLimit.value()

    val childRatio = childElementsProcessed / childStreamN
    val parentRatio = parentElementsProcessed / parentStreamN

    (childRatio + parentRatio, childRatio + 0.5, parentRatio + 0.5)

  }


}
