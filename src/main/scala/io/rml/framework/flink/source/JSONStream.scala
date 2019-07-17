package io.rml.framework.flink.source

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.{FileStream, KafkaStream, Literal, StreamDataSource, TCPSocketStream}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

case class JSONStream(stream: DataStream[Iterable[Item]]) extends Stream

object JSONStream extends Logging {
  val DEFAULT_PATH_OPTION: String = "$"

  def apply(source: StreamDataSource, iter: List[Option[Literal]])(implicit env: StreamExecutionEnvironment): Stream = {
    val iteratorList = iter.map({
      case Some(x) => x.toString
      case _ => DEFAULT_PATH_OPTION
    })
      .distinct

    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream, iteratorList)
      case fileStream: FileStream => fromFileStream(fileStream.path, iteratorList)
      case kafkaStream: KafkaStream => fromKafkaStream(kafkaStream, iteratorList)
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream, iterator: List[String])(implicit env: StreamExecutionEnvironment): JSONStream = {
    val stream: DataStream[Iterable[Item]] = StreamUtil.createTcpSocketSource(tCPSocketStream)
      .map { item =>
        JSONItem.fromStringOptionableList(item, iterator)
      }

    JSONStream(stream)
  }

  def fromFileStream(path: String, jsonPath: List[String])(implicit env: StreamExecutionEnvironment): JSONStream = {
    val stream: DataStream[Iterable[Item]] = env.createInput(new JSONInputFormat(path, jsonPath))
    JSONStream(stream)
  }

  def fromKafkaStream(kafkaStream: KafkaStream, iterator: List[String])(implicit env: StreamExecutionEnvironment): JSONStream = {
    val properties = kafkaStream.getProperties
    val consumer = kafkaStream.getConnectorFactory.getSource(kafkaStream.topic, new SimpleStringSchema(), properties)

    logDebug(consumer.getProducedType.toString)
    val stream: DataStream[Iterable[Item]] = env.addSource(consumer)
      .map { item =>
        JSONItem.fromStringOptionableList(item, iterator)
      }
    JSONStream(stream)
  }
}
