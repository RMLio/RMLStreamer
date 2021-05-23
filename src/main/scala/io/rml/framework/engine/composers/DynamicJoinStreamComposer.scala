package io.rml.framework.engine.composers

import io.rml.framework.core.model.JoinedTriplesMap
import io.rml.framework.engine.PostProcessor
import io.rml.framework.engine.windows.DynamicWindowImpl
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

class DynamicJoinStreamComposer[T <: Iterable[Item], U <: Iterable[Item]](childStream: DataStream[T], parentStream: DataStream[U], tm: JoinedTriplesMap) extends
  StreamJoinComposer
    [T, U, Iterable[JoinedItem], TimeWindow](childStream, parentStream, tm) {

  override def composeStreamJoin()(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment, postProcessor: PostProcessor): DataStream[Iterable[JoinedItem]] = {

    val joinCondition = tm.joinCondition.get

    childStream.connect(parentStream)
      .keyBy(keySelectorWithJoinCondition(joinCondition.child.map(_.value)), keySelectorWithJoinCondition(joinCondition.parent.map(_.value)))
      .process[String, Iterable[JoinedItem]](new DynamicWindowImpl()).name("DynamicWindow join")
  }
}
