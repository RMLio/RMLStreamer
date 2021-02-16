package io.rml.framework.engine.windows

import java.util
import java.util.Collections

import org.apache.flink.api.common.ExecutionConfig
import org.apache.flink.api.common.time.Time
import org.apache.flink.api.common.typeutils.TypeSerializer
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner
import org.apache.flink.streaming.api.windowing.triggers.{EventTimeTrigger, Trigger}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.api.windowing.windows.TimeWindow.Serializer

class VCTWindowAssigner(val isEventTime:Boolean) extends WindowAssigner[Object, TimeWindow]{

  private var PREVIOUS_UPDATE_TIME = 0L
  //Every 5 seconds the velocity join cost will be calculated and windows updated dynamically
  private val UPDATE_PERIOD = 5000L


  override def assignWindows(t: Object, timestamp: Long, ctxt: WindowAssigner.WindowAssignerContext): util.Collection[TimeWindow] = {
    if (timestamp <= -9223372036854775808L) {
      throw new RuntimeException("Record has Long.MIN_VALUE timestamp (= no timestamp marker). Is the time characteristic set to 'ProcessingTime', or did you forget to call 'DataStream.assignTimestampsAndWatermarks(...)'?")
    }

    //Update the windows time slices
    if(timestamp - PREVIOUS_UPDATE_TIME >= UPDATE_PERIOD){
      PREVIOUS_UPDATE_TIME = timestamp


    }
    ???
    }

  override def getDefaultTrigger(streamExecutionEnvironment: StreamExecutionEnvironment): Trigger[Object, TimeWindow] = {
    EventTimeTrigger.create()
  }

  override def getWindowSerializer(executionConfig: ExecutionConfig): TypeSerializer[TimeWindow] = {
    new Serializer()
  }

}
