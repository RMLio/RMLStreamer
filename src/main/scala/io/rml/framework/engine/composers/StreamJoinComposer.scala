package io.rml.framework.engine.composers

import io.rml.framework.core.model.{JoinedTriplesMap, TriplesMap}
import io.rml.framework.engine.PostProcessor
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, WindowAssigner}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.{TimeWindow, Window}

abstract class StreamJoinComposer[T <: Iterable[Item],U <: Iterable[Item], V, W <: Window]
(R:DataStream[T],
 S:DataStream[U],
 tm: JoinedTriplesMap,
 windowAssigner: WindowAssigner[Object,W] =  TumblingEventTimeWindows.of(Time.milliseconds(20))) {
     def composeStreamJoin()(implicit env: ExecutionEnvironment,
                                     senv: StreamExecutionEnvironment,
                                     postProcessor: PostProcessor):DataStream[V]
}




object StreamJoinComposer {
  //Default stream join composer
  def apply[T <: Iterable[Item], U <: Iterable[Item]]
  (stream:DataStream[T], stream2: DataStream[U], tm: JoinedTriplesMap, joinType: JoinType = TumblingJoin )
  (implicit env: ExecutionEnvironment,
   senv: StreamExecutionEnvironment,
   postProcessor: PostProcessor):
  StreamJoinComposer[T, U, JoinedItem, TimeWindow] ={


    joinType match  {
      case TumblingJoin =>  new TumblingStreamJoinComposer[T, U](stream, stream2, tm)
      case CrossJoin => new CrossJoinStreamComposer[T, U](stream, stream2, tm)
      case _ => new TumblingStreamJoinComposer[T,U](stream, stream2, tm)
    }

  }



}
