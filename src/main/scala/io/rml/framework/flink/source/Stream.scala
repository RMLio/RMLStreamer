package io.rml.framework.flink.source

import io.rml.framework.flink.item.Item
import org.apache.flink.streaming.api.scala.DataStream

abstract class Stream extends Source {
  def stream : DataStream[Item]
}

