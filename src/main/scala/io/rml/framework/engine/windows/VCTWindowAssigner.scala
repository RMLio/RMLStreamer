package io.rml.framework.engine.windows

import java.util

import org.apache.flink.api.common.ExecutionConfig
import org.apache.flink.api.common.typeutils.TypeSerializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner
import org.apache.flink.streaming.api.windowing.triggers.Trigger
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

class VCTWindowAssigner(val isEventTime:Boolean) extends WindowAssigner[Object, TimeWindow]{
  override def assignWindows(t: Object, l: Long, windowAssignerContext: WindowAssigner.WindowAssignerContext): util.Collection[TimeWindow] = ???

  override def getDefaultTrigger(streamExecutionEnvironment: StreamExecutionEnvironment): Trigger[Object, TimeWindow] = ???

  override def getWindowSerializer(executionConfig: ExecutionConfig): TypeSerializer[TimeWindow] = ???

}
