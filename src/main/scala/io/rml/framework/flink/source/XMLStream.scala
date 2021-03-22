/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/
package io.rml.framework.flink.source

import io.rml.framework.core.item.Item
import io.rml.framework.core.item.xml.XMLItem
import io.rml.framework.core.model.{FileStream, KafkaStream, StreamDataSource, TCPSocketStream}
import io.rml.framework.flink.connector.kafka.UniversalKafkaConnectorFactory
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.slf4j.LoggerFactory

case class XMLStream(stream: DataStream[Iterable[Item]]) extends Stream

object XMLStream {

  def apply(source: StreamDataSource, xpaths: List[String])(implicit env: StreamExecutionEnvironment): Stream = {

    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream, xpaths)
      case fileStream: FileStream => fromFileStream(fileStream.path, xpaths)
      case kafkaStream: KafkaStream => fromKafkaStream(kafkaStream, xpaths)
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream, xpaths: List[String])(implicit env: StreamExecutionEnvironment): XMLStream = {
    val stream: DataStream[Iterable[Item]] = StreamUtil.paralleliseOverSlots(StreamUtil.createTcpSocketSource(tCPSocketStream))
      .map(item => {
        XMLItem.fromStringOptionable(item, xpaths)
      })
    XMLStream(stream)
  }

  def fromFileStream(path: String, xpaths: List[String])(implicit senv: StreamExecutionEnvironment): XMLStream = {
    val source = new XMLSource(path, xpaths)
    XMLStream(senv.addSource(source))
    throw new NotImplementedError("FileStream is not implemented properly yet ")

  }

  def fromKafkaStream(kafkaStream: KafkaStream, xpaths: List[String])(implicit env: StreamExecutionEnvironment): XMLStream = {
    val properties = kafkaStream.getProperties
    val consumer = UniversalKafkaConnectorFactory.getSource(kafkaStream.topic, new SimpleStringSchema(), properties)
    val stream: DataStream[Iterable[Item]] = StreamUtil.paralleliseOverSlots(env.addSource(consumer))
      .map(item => {
        XMLItem.fromStringOptionable(item, xpaths)
      })
    XMLStream(stream)
  }

}

class XMLSource(path: String, xpaths: List[String]) extends SourceFunction[Iterable[Item]] {

  val serialVersionUID = 1L
  @volatile private var isRunning = true
  private val LOG = LoggerFactory.getLogger(classOf[XMLSource])

  override def cancel(): Unit = isRunning = false

  override def run(ctx: SourceFunction.SourceContext[Iterable[Item]]): Unit = {

  }
}
