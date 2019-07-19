package io.rml.framework.flink.source

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.{FileStream, KafkaStream, Literal, StreamDataSource, TCPSocketStream}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

case class JSONStream(stream: DataStream[Iterable[Item]]) extends Stream

object JSONStream extends Logging {
  val DEFAULT_PATH_OPTION: String = Source.DEFAULT_ITERATOR_MAP(RMLVoc.Class.JSONPATH).getOrElse("$")

  def apply(source: StreamDataSource, jsonPaths: List[Option[Literal]])(implicit env: StreamExecutionEnvironment): Stream = {
    val jsonPathStrings = jsonPaths.map({
      case Some(x) => x.toString
      case _ => DEFAULT_PATH_OPTION
    })
      .distinct

    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream, jsonPathStrings)
      case fileStream: FileStream => fromFileStream(fileStream.path, jsonPathStrings)
      case kafkaStream: KafkaStream => fromKafkaStream(kafkaStream, jsonPathStrings)
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream, jsonPaths: List[String])(implicit env: StreamExecutionEnvironment): JSONStream = {
    val stream: DataStream[Iterable[Item]] = StreamUtil.createTcpSocketSource(tCPSocketStream)
      .map { item =>
        JSONItem.fromStringOptionableList(item, jsonPaths)
      }

    JSONStream(stream)
  }

  def fromFileStream(path: String, jsonPaths: List[String])(implicit env: StreamExecutionEnvironment): JSONStream = {
    //val stream: DataStream[Iterable[Item]] = env.createInput(new JSONInputFormat(path, jsonPaths.head))
    //JSONStream(stream)
    throw new NotImplementedError("FileStream is not implemented properly yet ")

  }

  def fromKafkaStream(kafkaStream: KafkaStream, jsonPaths: List[String])(implicit env: StreamExecutionEnvironment): JSONStream = {
    val properties = kafkaStream.getProperties
    val consumer = kafkaStream.getConnectorFactory.getSource(kafkaStream.topic, new SimpleStringSchema(), properties)

    logDebug(consumer.getProducedType.toString)
    val stream: DataStream[Iterable[Item]] = env.addSource(consumer)
      .map { item =>
        JSONItem.fromStringOptionableList(item, jsonPaths)
      }
    JSONStream(stream)
  }
}
