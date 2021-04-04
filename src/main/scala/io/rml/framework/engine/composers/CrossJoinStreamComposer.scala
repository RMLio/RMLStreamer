package io.rml.framework.engine.composers

import io.rml.framework.core.model.JoinedTriplesMap
import io.rml.framework.engine.PostProcessor
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

class CrossJoinStreamComposer[T <: Iterable[Item], U <: Iterable[Item]]
(R: DataStream[T], S: DataStream[U], tm: JoinedTriplesMap)
  extends StreamJoinComposer[T, U, Iterable[JoinedItem], TimeWindow](R, S, tm) {
  override def composeStreamJoin()
                                (implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment, postProcessor: PostProcessor): DataStream[Iterable[JoinedItem]] = {
    // if there are no join conditions a cross join will be executed
    // add pseudo key for all incoming elements
    val key = tm.logicalSource.semanticIdentifier
    // create a cross join data stream where all items are joined
    // on a default fixed key
    val crossed = R.join(S)
      .where(_ => key)
      .equalTo(_ => key)
      .window(this.windowAssigner)

    crossed.apply((firstIterItems, secondIterItems) => {
      // mini cross join for all iteritems
      firstIterItems.flatMap(item1 => secondIterItems.map(item2 => JoinedItem(item1, item2)))
    })

  }
}
