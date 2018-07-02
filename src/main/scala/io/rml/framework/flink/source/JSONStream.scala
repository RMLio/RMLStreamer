package io.rml.framework.flink.source

import java.util.Properties

import io.rml.framework.core.model.{FileStream, KafkaStream, StreamDataSource, TCPSocketStream}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

case class JSONStream(stream: DataStream[Item]) extends Stream

object JSONStream {

  def apply(source:StreamDataSource, iterator : String)(implicit env: StreamExecutionEnvironment)  : Stream = {
    source match {
      case tcpStream : TCPSocketStream  => fromTCPSocketStream(tcpStream)
      case fileStream : FileStream => fromFileStream(fileStream.path, iterator)
      case kafkaStream : KafkaStream => fromKafkaStream(kafkaStream)
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream)(implicit env: StreamExecutionEnvironment) : JSONStream = {
    val stream: DataStream[Item] = StreamUtil.createTcpSocketSource(tCPSocketStream)
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
