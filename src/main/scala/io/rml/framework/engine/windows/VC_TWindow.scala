package io.rml.framework.engine.windows

import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.time.Time
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
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
class VC_TWindow[T <: Iterable[Item], U <: Iterable[Item]](val updateCycle: Time = Time.seconds(10)) extends KeyedCoProcessFunction[String, T, U, JoinedItem] {


  //Holds the map state of incoming child tuples that are still alive in the window
  lazy val childMapState: MapState[Long, Iterable[Item]] = getRuntimeContext.getMapState(
    new MapStateDescriptor[Long, Iterable[Item]]("VC_TWJoinChild", classOf[Long], classOf[Iterable[Item]])
  )

  //Holds the map state of incoming parent tuples that are still alive in the window
  lazy val parentMapState: MapState[Long, Iterable[Item]] = getRuntimeContext.getMapState(
    new MapStateDescriptor[Long, Iterable[Item]]("VC_TWJoinParent", classOf[Long], classOf[Iterable[Item]])
  )


  lazy val lastCheckup: ValueState[Time] = getRuntimeContext.getState(
    new ValueStateDescriptor[Time]("lastCheckup", classOf[Time])
  )


  lazy val checkUpFired: ValueState[Boolean] = getRuntimeContext.getState(
    new ValueStateDescriptor[Boolean]("isWindowUpdated", classOf[Boolean])
  )

  private def crossJoin(elementIter: Iterable[Item], isChild: Boolean, timeStamp: Long, thisMapState: MapState[Long, Iterable[Item]], thatMapState: MapState[Long, Iterable[Item]]):
  Iterable[JoinedItem] = {
    thisMapState.put(timeStamp, elementIter)

    thatMapState.values()
      .flatMap(thatElement =>
        thatElement.flatMap(item1 =>
          elementIter.map(item2 =>
            if (isChild)
              JoinedItem(item2, item1)
            else
              JoinedItem(item1, item2)
          )
        )
      )


  }
  private def fireWindowUpdateCallback(context: KeyedCoProcessFunction[String, T, U, JoinedItem]#Context): Unit = {
    if (!checkUpFired.value()) {
      context.timerService.registerEventTimeTimer(lastCheckup.value().toMilliseconds + updateCycle.toMilliseconds)
      checkUpFired.update(true)
    }
  }

  override def processElement1(child: T, context: KeyedCoProcessFunction[String, T, U, JoinedItem]#Context, collector: Collector[JoinedItem]): Unit = {

    crossJoin(child, isChild = true, context.timestamp(), childMapState, parentMapState)
    fireWindowUpdateCallback(context)
  }




  override def processElement2(parent: U, context: KeyedCoProcessFunction[String, T, U, JoinedItem]#Context, collector: Collector[JoinedItem]): Unit = {

    crossJoin(parent, isChild = false, context.timestamp(), parentMapState, childMapState)
    fireWindowUpdateCallback(context)
  }

  //Cyclic update callback to update window sizes
  override def onTimer(timestamp: Long, ctx: KeyedCoProcessFunction[String, T, U, JoinedItem]#OnTimerContext, out: Collector[JoinedItem]): Unit = {

  }

}
