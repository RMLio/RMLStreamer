package io.rml.framework.flink.source

import java.util.Properties

import io.rml.framework.core.model.KafkaStream
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.api.common.io.FileInputFormat
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

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

  def fromKafkaStream(kafkaStream: KafkaStream)(implicit env: StreamExecutionEnvironment) : JSONStream = {
    val properties = new Properties()
    val brokersCommaSeparated = kafkaStream.brokers.reduce((a,b) => a + ", " + b)
    properties.setProperty("bootstrap.servers", brokersCommaSeparated)
    val zookeepersCommaSeparated = kafkaStream.zookeepers.reduce((a,b) => a + ", " + b)
    properties.setProperty("zookeepers.connect", zookeepersCommaSeparated)
    properties.setProperty("group.id", kafkaStream.groupId)
    val stream: DataStream[Item] = env.addSource(new FlinkKafkaConsumer08[String](kafkaStream.topic, new SimpleStringSchema(), properties))
                                      .map(item => { JSONItem.fromString(item)})
    JSONStream(stream)
  }
}
