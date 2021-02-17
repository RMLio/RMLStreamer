package io.rml.framework.engine.composers

import io.rml.framework.core.model.JoinedTriplesMap
import io.rml.framework.engine.PostProcessor
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

class TumblingStreamJoinComposer[T <: Iterable[Item], U <: Iterable[Item]](R: DataStream[T], S: DataStream[U], tm: JoinedTriplesMap) extends
  StreamJoinComposer
    [T, U, JoinedItem, TimeWindow](R, S, tm) {


  override def composeStreamJoin()(implicit env: ExecutionEnvironment,
                                   senv: StreamExecutionEnvironment,
                                   postProcessor: PostProcessor): DataStream[JoinedItem] = {
    val childDataStream = R
    val parentDataStream = S
    val joined = childDataStream
      .join(parentDataStream)
      .where(iterItems =>
        //Key selector assigns the iterItems if one of the item in the iterables satisfy the join condition
        //This is due to the possibility of one raw input generating multiple items by the RML
        //reference formulation iterators
        iterItems
          .map(item => item.refer(tm.joinCondition.get.child.toString))
          .flatten(o => o.get)
          .head
      )
      .equalTo(iterItems =>
        iterItems
          .map(item => item.refer(tm.joinCondition.get.parent.toString))
          .flatten(o => o.get)
          .head
      )
      .window(this.windowAssigner)


    joined.apply((firstIterItems, secondIterItems) => {
      firstIterItems.flatMap(item1 => secondIterItems.map(item2 => JoinedItem(item1, item2)))
    })
      .flatMap(joined => joined)
  }
}
