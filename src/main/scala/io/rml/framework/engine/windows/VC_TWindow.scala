package io.rml.framework.engine.windows

import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.collection.JavaConversions.iterableAsScalaIterable

/**
 * VC_TWindow receives tuples already keyed based on their joining attributes, therefore
 * we could just execute a cross join inside the window accordingly to get the resulting list of JoinedItems.
 *
 * @param updateCycle fixed cycle where the window length will be updated according to the calculations
 * @tparam T subtypes of Iterable[Item]
 * @tparam U subtypes of Iterable[Item]
 */
class VC_TWindow[T <: Iterable[Item], U <: Iterable[Item]](val epsilon:Double = 1.5,
                                                          val defaultBucketSize:Long = 1000L,
                                                           val windowSize: Time = Time.seconds(2),
                                                           val updateCycle: Time = Time.seconds(10))
  extends KeyedCoProcessFunction[String, T, U, JoinedItem] {


  //Holds the map state of incoming child tuples that are still alive in the window
  lazy val childMapState: MapState[Long, Iterable[Item]] = getRuntimeContext.getMapState(
    new MapStateDescriptor[Long, Iterable[Item]]("VC_TWJoinChild", classOf[Long], classOf[Iterable[Item]])
  )

  //Holds the map state of incoming parent tuples that are still alive in the window
  lazy val parentMapState: MapState[Long, Iterable[Item]] = getRuntimeContext.getMapState(
    new MapStateDescriptor[Long, Iterable[Item]]("VC_TWJoinParent", classOf[Long], classOf[Iterable[Item]])
  )

  //Holds the timestamp when the window was last updated
  lazy val lastCheckup: ValueState[Time] = getRuntimeContext.getState(
    new ValueStateDescriptor[Time]("lastCheckup", classOf[Time])
  )

  //Size of the window usd by VC_TWindow to join the incoming tuples
  //TODO: is this necessary?
  lazy val windowTimeSlice: ValueState[TimeWindow] = getRuntimeContext.getState(
    new ValueStateDescriptor[TimeWindow]("timeSlice", classOf[TimeWindow])
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


  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    isCallBackFired.update(false)
    lastCheckup.update(Time.milliseconds(-10L))
    dynamicUpdateCycle.update(updateCycle)
    pseudoParentLimit.update(defaultBucketSize)
    pseudoChildLimit.update(defaultBucketSize)

  }

  override def processElement1(child: T, context: KeyedCoProcessFunction[String, T, U, JoinedItem]#Context, collector: Collector[JoinedItem]): Unit = {
    val joinedItems = crossJoin(child, isChild = true, context.timestamp(), childMapState, parentMapState)

    joinedItems.foreach(collector.collect)
    fireWindowUpdateCallback(context)
  }

  override def processElement2(parent: U, context: KeyedCoProcessFunction[String, T, U, JoinedItem]#Context, collector: Collector[JoinedItem]): Unit = {

    val joinedItems = crossJoin(parent, isChild = false, context.timestamp(), parentMapState, childMapState)
    joinedItems.foreach(collector.collect)
    fireWindowUpdateCallback(context)
  }

  //TODO: Move this join function out of this class, such that the window can be provided with a 
  //higher order function to be executed on the incoming elements. 
  private def crossJoin(elementIter: Iterable[Item], isChild: Boolean, timeStamp: Long, thisMapState: MapState[Long, Iterable[Item]], thatMapState: MapState[Long, Iterable[Item]]):
  Iterable[JoinedItem] = {
    thisMapState.put(timeStamp, elementIter)

    thatMapState.values()
      .flatMap(thatElementIter =>
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
  private def fireWindowUpdateCallback(context: KeyedCoProcessFunction[String, T, U, JoinedItem]#Context): Unit = {
    if(lastCheckup.value().getSize < 0L){
      lastCheckup.update(Time.milliseconds(context.timestamp()))
    }

    if (!isCallBackFired.value()) {
      context.timerService.registerEventTimeTimer(lastCheckup.value().toMilliseconds + dynamicUpdateCycle.value().toMilliseconds)
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
  override def onTimer(timestamp: Long, ctx: KeyedCoProcessFunction[String, T, U, JoinedItem]#OnTimerContext, out: Collector[JoinedItem]): Unit = {

    val start = TimeWindow.getWindowStartWithOffset(timestamp, 0L, windowSize.toMilliseconds)
    windowTimeSlice.update(new TimeWindow(start, start + windowSize.toMilliseconds))
    val (join_entropy, childRatio, parentRatio) = calculateJoinCost()

    //Adjust pseudo limit based on the calculated ratios
    pseudoChildLimit.update((pseudoChildLimit.value() * childRatio).toLong)
    pseudoParentLimit.update((pseudoParentLimit.value() * parentRatio).toLong)

    val currentDynamicUpdateCycle = dynamicUpdateCycle.value()
    if (join_entropy > epsilon && join_entropy > 0){
      dynamicUpdateCycle.update(Time.milliseconds( currentDynamicUpdateCycle.toMilliseconds/2))
    }else{
      dynamicUpdateCycle.update(Time.milliseconds(currentDynamicUpdateCycle.toMilliseconds*2))
    }

    lastCheckup.update(Time.milliseconds(timestamp))
    cleanUpWindow()
  }


  private def cleanUpWindow(): Unit = {
    childMapState.clear()
    parentMapState.clear()

    isCallBackFired.update(false)
  }


  /**
   * Calculates the join entropy based on the paper VC-TWJoin: A Stream Join Algorithm Based onVariable Update Cycle Time Window
   * @return
   */
  private def calculateJoinCost(): (Double, Double, Double) = {



    val childElementsProcessed: Double = childMapState.values().size
    val parentElementsProcessed: Double = parentMapState.values().size
    val childStreamN: Double = pseudoChildLimit.value()
    val parentStreamN: Double = pseudoParentLimit.value()

    val childRatio = childElementsProcessed/childStreamN
    val parentRatio = parentElementsProcessed/parentStreamN

    (childRatio + parentRatio, childRatio, parentRatio)

  }





}
