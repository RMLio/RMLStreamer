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

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.item.Item
import io.rml.framework.core.item.json.JSONItem
import io.rml.framework.core.model._
import io.rml.framework.flink.connector.kafka.UniversalKafkaConnectorFactory
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

case class JSONStream(val stream: DataStream[Iterable[Item]]) extends Stream

object JSONStream extends Logging {

  def apply(source: StreamDataSource, jsonPaths: List[String])(implicit env: StreamExecutionEnvironment): Stream = {

    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream, jsonPaths)
      case fileStream: FileStream => fromFileStream(fileStream.path, jsonPaths)
      case kafkaStream: KafkaStream => fromKafkaStream(kafkaStream, jsonPaths)
      case mqttStream : MQTTStream => fromMQTTStream(mqttStream, jsonPaths)
      case wsStream: WsStream => fromWsStream(wsStream, jsonPaths)
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream, jsonPaths: List[String])(implicit env: StreamExecutionEnvironment): JSONStream = {
    val socketStream = StreamUtil.paralleliseOverSlots(StreamUtil.createTcpSocketSource(tCPSocketStream))
    val stream: DataStream[Iterable[Item]] = socketStream
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
    val consumer = UniversalKafkaConnectorFactory.getSource(kafkaStream.topic, new SimpleStringSchema(), properties)

    logDebug(consumer.getProducedType.toString)
    streamFromSource(consumer, jsonPaths, env)
  }

  def fromMQTTStream(mqttStream : MQTTStream, jsonPaths : List[String])(implicit env: StreamExecutionEnvironment):JSONStream = {
    val source = RichMQTTSource(
      mqttStream.hypermediaTarget,
      mqttStream.contentType,
      mqttStream.controlPacketValue,
      mqttStream.dup,
      mqttStream.qos
    )
    streamFromSource(source, jsonPaths, env)
  }

  def fromWsStream(wsStream: WsStream, jsonPaths : List[String])(implicit env: StreamExecutionEnvironment):JSONStream = {
    logDebug("From websocket" + wsStream.toString)
    val source = new WebSocketSource(wsStream.hypermediaTarget)
    streamFromSource(source, jsonPaths, env)
  }

  private def streamFromSource(source: SourceFunction[String],
                                                    jsonPaths: List[String],
                                                    env: StreamExecutionEnvironment): JSONStream = {
    val parallelStream = StreamUtil.paralleliseOverSlots(env.addSource(source))
    val stream: DataStream[Iterable[Item]] = parallelStream
      .map { item =>
        JSONItem.fromStringOptionableList(item, jsonPaths)
      }

    JSONStream(stream)
  }
}
