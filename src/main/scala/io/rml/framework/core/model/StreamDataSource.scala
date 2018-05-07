/*
 * Copyright (c) 2017 Ghent University - imec
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.rml.framework.core.model

import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.flink.source.{CSVStream, JSONStream, Stream, XMLStream}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._


trait StreamDataSource extends DataSource

object StreamDataSource {
  def fromLogicalSource(logicalSource: LogicalSource)(implicit env: StreamExecutionEnvironment): Stream = {
    val source = logicalSource.source match {
      case tcpStream : TCPSocketStream =>
        val hostName = tcpStream.hostName
        val port = tcpStream.port
        logicalSource.referenceFormulation match {
          case Uri(RMLVoc.Class.CSV) => CSVStream.fromTCPSocketStream(hostName, port, Array(""))
          case Uri(RMLVoc.Class.XPATH) => XMLStream.fromTCPSocketStream(hostName, port)
          case Uri(RMLVoc.Class.JSONPATH) => JSONStream.fromTCPSocketStream(hostName, port)
        }
      case fileStream : FileStream =>
        val path = fileStream.path
        val iterator = logicalSource.iterator.get.value
        logicalSource.referenceFormulation match {
          case Uri(RMLVoc.Class.CSV) => CSVStream.fromFileStream(path)
          case Uri(RMLVoc.Class.XPATH) => XMLStream.fromFileStream(path, iterator)
          case Uri(RMLVoc.Class.JSONPATH) => JSONStream.fromFileStream(path, iterator)
        }
      case kafkaStream: KafkaStream => {
        logicalSource.referenceFormulation match {
          case Uri(RMLVoc.Class.CSV) => CSVStream.fromKafkaStream(kafkaStream, null) //TODO headers!
          case Uri(RMLVoc.Class.XPATH) => XMLStream.fromKafkaStream(kafkaStream)
          case Uri(RMLVoc.Class.JSONPATH) => JSONStream.fromKafkaStream(kafkaStream)
        }
      }
    }
    source
  }
}
