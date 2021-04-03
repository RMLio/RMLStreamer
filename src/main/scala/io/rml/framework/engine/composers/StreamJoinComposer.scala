package io.rml.framework.engine.composers

import io.rml.framework.core.model.{JoinConfigMap, JoinedTriplesMap, TriplesMap}
import io.rml.framework.engine.PostProcessor
import io.rml.framework.flink.item.{Item, JoinedItem}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, WindowAssigner}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.{TimeWindow, Window}

abstract class StreamJoinComposer[T <: Iterable[Item],U <: Iterable[Item], V, W <: Window]
(childStream:DataStream[T],
 parentStream:DataStream[U],
 tm: JoinedTriplesMap,
 val windowAssigner: WindowAssigner[Object,TimeWindow] =  TumblingEventTimeWindows.of(Time.milliseconds(20))) {



     def composeStreamJoin()(implicit env: ExecutionEnvironment,
                                     senv: StreamExecutionEnvironment,
                                     postProcessor: PostProcessor):DataStream[V]
}




object StreamJoinComposer {
  //Default stream join composer
  def apply[T <: Iterable[Item], U <: Iterable[Item]]
  (stream:DataStream[T], stream2: DataStream[U], tm: JoinedTriplesMap, joinConfigMap: JoinConfigMap )
  (implicit env: ExecutionEnvironment,
   senv: StreamExecutionEnvironment,
   postProcessor: PostProcessor):
  StreamJoinComposer[T, U, JoinedItem, TimeWindow] ={


    val joinType = joinConfigMap.joinType

    joinType match  {
      case VC_TWindowJoin => new VC_TWJoinStreamComposer(stream, stream2, tm)
      case TumblingJoin =>  new TumblingJoinStreamComposer(stream, stream2, tm)
      case CrossJoin => new CrossJoinStreamComposer(stream, stream2, tm)
      case _ => new TumblingJoinStreamComposer(stream, stream2, tm)
    }

  }



}