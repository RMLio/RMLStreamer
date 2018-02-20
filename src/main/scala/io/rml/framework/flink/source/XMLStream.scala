package io.rml.framework.flink.source

import com.ximpleware.extended.AutoPilotHuge
import com.ximpleware.{AutoPilot, VTDGen}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.xml.XMLItem
import io.rml.framework.shared.RMLException
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.slf4j.LoggerFactory

case class XMLStream(stream: DataStream[Item]) extends Stream

object XMLStream {

  def fromTCPSocketStream(hostName: String, port: Int)(implicit env: StreamExecutionEnvironment) : XMLStream = {
    val stream = env.socketTextStream(hostName, port)
                    .map(item => XMLItem.fromString(item).asInstanceOf[Item])
    XMLStream(stream)
  }

  def fromFileStream(path: String, xpath: String)(implicit senv: StreamExecutionEnvironment) : XMLStream = {
    val source = new XMLSource(path, xpath)
    XMLStream(senv.addSource(source))
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
