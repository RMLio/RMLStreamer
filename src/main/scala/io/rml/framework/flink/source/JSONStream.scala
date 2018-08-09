package io.rml.framework.flink.source

import java.util.Properties

import io.rml.framework.core.model.{FileStream, KafkaStream, StreamDataSource, TCPSocketStream}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

case class JSONStream(stream: DataStream[Item]) extends Stream

object JSONStream {

  def apply(source: StreamDataSource, iter: Option[String])(implicit env: StreamExecutionEnvironment): Stream = {
    val iterator =  iter.getOrElse("$")
    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream, iterator)
      case fileStream: FileStream => fromFileStream(fileStream.path, iterator)
      case kafkaStream: KafkaStream => fromKafkaStream(kafkaStream, iterator)
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream, iterator:String)(implicit env: StreamExecutionEnvironment): JSONStream = {
    val stream: DataStream[Item] = StreamUtil.createTcpSocketSource(tCPSocketStream)
      .flatMap(item => {
        JSONItem.fromStringOptionableList(item, iterator)
      })
      .flatMap(item => {
        item
      })
    JSONStream(stream)
  }

  def fromFileStream(path: String, jsonPath: String)(implicit env: StreamExecutionEnvironment): JSONStream = {
    val stream: DataStream[Item] = env.createInput(new JSONInputFormat(path, jsonPath))
    JSONStream(stream)
  }

  def fromKafkaStream(kafkaStream: KafkaStream, iterator:String)(implicit env: StreamExecutionEnvironment): JSONStream = {
    val properties = new Properties()
    val brokersCommaSeparated = kafkaStream.brokers.reduce((a, b) => a + ", " + b)
    properties.setProperty("bootstrap.servers", brokersCommaSeparated)
    val zookeepersCommaSeparated = kafkaStream.zookeepers.reduce((a, b) => a + ", " + b)
    properties.setProperty("zookeeper.connect", zookeepersCommaSeparated)
    properties.setProperty("group.id", kafkaStream.groupId)
    properties.setProperty("auto.offset.reset", "earliest")

    val stream: DataStream[Item] = env.addSource(new FlinkKafkaConsumer010[String](kafkaStream.topic, new SimpleStringSchema(), properties))
      .flatMap(item => {
        JSONItem.fromStringOptionableList(item, iterator)
      })
        .flatMap( item => item)
    JSONStream(stream)
  }
}
