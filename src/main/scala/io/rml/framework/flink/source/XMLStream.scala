package io.rml.framework.flink.source

import io.rml.framework.core.model.{FileStream, KafkaStream, StreamDataSource, TCPSocketStream}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.xml.XMLItem
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.slf4j.LoggerFactory

case class XMLStream(stream: DataStream[Iterable[Item]]) extends Stream

object XMLStream {
  val DEFAULT_PATH_OPTION = "/"

  def apply(source: StreamDataSource, iter: List[Option[String]])(implicit env: StreamExecutionEnvironment): Stream = {
    val iterator = iter.map({
      case Some(x) => x.toString
      case _ => DEFAULT_PATH_OPTION
    })
      .distinct


    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream, iterator)
      case fileStream: FileStream => fromFileStream(fileStream.path, iterator)
      case kafkaStream: KafkaStream => fromKafkaStream(kafkaStream, iterator)
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream, iterator: List[String])(implicit env: StreamExecutionEnvironment): XMLStream = {
    val stream: DataStream[Iterable[Item]] = StreamUtil.createTcpSocketSource(tCPSocketStream)
      .map(item => {
        XMLItem.fromStringOptionable(item, iterator)
      })
    XMLStream(stream)
  }

  def fromFileStream(path: String, xpath: List[String])(implicit senv: StreamExecutionEnvironment): XMLStream = {
    val source = new XMLSource(path, xpath)
    XMLStream(senv.addSource(source))
  }

  def fromKafkaStream(kafkaStream: KafkaStream, iterator: List[String])(implicit env: StreamExecutionEnvironment): XMLStream = {
    val properties = kafkaStream.getProperties
    val consumer = kafkaStream.getConnectorFactory.getSource(kafkaStream.topic, new SimpleStringSchema(), properties)
    val stream: DataStream[Iterable[Item]] = env.addSource(consumer)
      .map(item => {
        XMLItem.fromStringOptionable(item, iterator)
      })
    XMLStream(stream)
  }

}

class XMLSource(path: String, xpath: String) extends SourceFunction[Item] {

  val serialVersionUID = 1L
  @volatile private var isRunning = true
  private val LOG = LoggerFactory.getLogger(classOf[XMLSource])

  override def cancel(): Unit = isRunning = false

  override def run(ctx: SourceFunction.SourceContext[Item]): Unit = {

  }
}
