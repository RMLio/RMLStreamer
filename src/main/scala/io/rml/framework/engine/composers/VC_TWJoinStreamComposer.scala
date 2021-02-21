package io.rml.framework.engine.composers

import io.rml.framework.core.model.JoinedTriplesMap
import io.rml.framework.engine.PostProcessor
import io.rml.framework.engine.windows.VC_TWindow
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

class VC_TWJoinStreamComposer[T <: Iterable[Item], U <: Iterable[Item]](childStream: DataStream[T], parentStream: DataStream[U], tm: JoinedTriplesMap) extends
  StreamJoinComposer
    [T, U, JoinedItem, TimeWindow](childStream, parentStream, tm) {

  override def composeStreamJoin()(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment, postProcessor: PostProcessor): DataStream[JoinedItem] = {
    val keySelectorWithJoinCondition = {
      (joinCondition: String) => {
        (iterItems:Iterable[Item]) =>
          iterItems
            .map(item => item.refer(joinCondition))
            .flatten(o => o.get)
            .head
      }
    }

    val joinCondition = tm.joinCondition.get

    childStream.connect(parentStream)
      .keyBy(keySelectorWithJoinCondition(joinCondition.child.toString), keySelectorWithJoinCondition(joinCondition.parent.toString))
      .process[String, JoinedItem](new VC_TWindow())
  }
}
