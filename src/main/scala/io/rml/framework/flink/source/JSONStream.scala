package io.rml.framework.flink.source

import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.api.common.io.FileInputFormat
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._

case class JSONStream(stream: DataStream[Item]) extends Stream

object JSONStream {
  def fromTCPSocketStream(hostName: String, port: Int)(implicit env: StreamExecutionEnvironment) : JSONStream = {
    val stream: DataStream[Item] = env.socketTextStream(hostName, port)
                                      .flatMap(item => {
                                        JSONItem.fromStringOptionable(item)
                                      })
                                      .map(item => {
                                        item.asInstanceOf[Item]
                                      })
    JSONStream(stream)
  }
  def fromFileStream(path: String, jsonPath: String)(implicit env: StreamExecutionEnvironment) : JSONStream = {
    val stream: DataStream[Item] = env.createInput(new JSONInputFormat(path, jsonPath))
    JSONStream(stream)
  }
}
