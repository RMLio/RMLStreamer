package io.rml.framework.core.windows

import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, WindowAssigner}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

object WindowAssignerFactory {
  var CHOSEN_WINDOW_ASSIGNER:Option[WindowAssigner[Object, TimeWindow]] = None
  private lazy val DEFAULT_WINDOW_ASSIGNER :WindowAssigner[Object, TimeWindow]  = TumblingEventTimeWindows.of(Time.milliseconds(2))

  def getWindowAssigner():WindowAssigner[Object, TimeWindow] = {
    CHOSEN_WINDOW_ASSIGNER.getOrElse(DEFAULT_WINDOW_ASSIGNER)
  }

}
