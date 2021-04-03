package io.rml.framework.engine.composers

import io.rml.framework.core.model.JoinedTriplesMap
import io.rml.framework.engine.PostProcessor
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

class TumblingJoinStreamComposer[T <: Iterable[Item], U <: Iterable[Item]](childStream: DataStream[T], parentStream: DataStream[U], tm: JoinedTriplesMap) extends
  StreamJoinComposer
    [T, U, JoinedItem, TimeWindow](childStream, parentStream, tm) {


  override def composeStreamJoin()(implicit env: ExecutionEnvironment,
                                   senv: StreamExecutionEnvironment,
                                   postProcessor: PostProcessor): DataStream[JoinedItem] = {
    val childDataStream = childStream
    val parentDataStream = parentStream
    val joined = childDataStream
      .join(parentDataStream)
      .where( keySelectorWithJoinCondition(tm.joinCondition.get.child.map(_.value)))
      .equalTo( keySelectorWithJoinCondition(tm.joinCondition.get.parent.map(_.value)))
      .window(this.windowAssigner)


    joined.apply((firstIterItems, secondIterItems) => {
      firstIterItems.flatMap(item1 => secondIterItems.map(item2 => JoinedItem(item1, item2)))
    })
      .flatMap(joined => joined)
  }
}
