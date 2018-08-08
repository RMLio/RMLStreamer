package io.rml.framework.flink.source

import java.util.Properties

import io.rml.framework.core.model.{FileStream, KafkaStream, StreamDataSource, TCPSocketStream}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.xml.XMLItem
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.slf4j.LoggerFactory

case class XMLStream(stream: DataStream[Item]) extends Stream

object XMLStream {

  def apply(source: StreamDataSource, iter: Option[String])(implicit env: StreamExecutionEnvironment): Stream = {

    val iterator =  iter.getOrElse("/*")

    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream, iterator)
      case fileStream: FileStream => fromFileStream(fileStream.path, iterator)
      case kafkaStream: KafkaStream => fromKafkaStream(kafkaStream)
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream,iterator:String)(implicit env: StreamExecutionEnvironment): XMLStream = {
    val stream: DataStream[Item] = StreamUtil.createTcpSocketSource(tCPSocketStream)
      .flatMap(item => {
        XMLItem.fromStringOptionable(item, iterator)
      }).flatMap( a => a )
    XMLStream(stream)
  }

  def fromFileStream(path: String, xpath: String)(implicit senv: StreamExecutionEnvironment): XMLStream = {
    val source = new XMLSource(path, xpath)
    XMLStream(senv.addSource(source))
  }

  def fromKafkaStream(kafkaStream: KafkaStream)(implicit env: StreamExecutionEnvironment): XMLStream = {
    val properties = new Properties()
    val brokersCommaSeparated = kafkaStream.brokers.reduce((a, b) => a + ", " + b)
    properties.setProperty("bootstrap.servers", brokersCommaSeparated)
    val zookeepersCommaSeparated = kafkaStream.zookeepers.reduce((a, b) => a + ", " + b)
    properties.setProperty("zookeeper.connect", zookeepersCommaSeparated)
    properties.setProperty("group.id", kafkaStream.groupId)
    val stream: DataStream[Item] = env.addSource(new FlinkKafkaConsumer08[String](kafkaStream.topic, new SimpleStringSchema(), properties))
      .map(item => {
        XMLItem.fromString(item).asInstanceOf[Item]
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
