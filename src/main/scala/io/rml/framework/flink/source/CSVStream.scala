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
import io.rml.framework.core.item.csv.CSVItem
import io.rml.framework.core.model.{FileStream, KafkaStream, StreamDataSource, TCPSocketStream}
import io.rml.framework.core.util.{CustomCSVConfig, DefaultCSVConfig}
import io.rml.framework.flink.connector.kafka.UniversalKafkaConnectorFactory
import org.apache.commons.csv.CSVFormat
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

case class CSVStream(stream: DataStream[Iterable[Item]] ) extends Stream

object CSVStream {

  def apply(source: StreamDataSource)(implicit env: StreamExecutionEnvironment): Stream = {

    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream)
      case fileStream: FileStream => fromFileStream(fileStream.path)
      case kafkaStream: KafkaStream => fromKafkaStream(kafkaStream)
      case _ => null
    }
  }

  def fromTCPSocketStream(tCPSocketStream: TCPSocketStream)(implicit env: StreamExecutionEnvironment): CSVStream = {
    // var's set up
    val defaultConfig = DefaultCSVConfig()
    val csvConfig = CustomCSVConfig(defaultConfig.delimiter, defaultConfig.quoteCharacter, "\n\n")


    // CSVFormat set up with delimiter and quote character
    val format = CSVFormat.newFormat(csvConfig.delimiter)
      .withQuote(csvConfig.quoteCharacter)
      .withTrim()
      .withFirstRecordAsHeader()

    val stream: DataStream[Iterable[Item]] = StreamUtil.paralleliseOverSlots(StreamUtil.createTcpSocketSource(tCPSocketStream, csvConfig.recordDelimiter))
      .map(batchString => {
        CSVItem.fromDataBatch(batchString, format)
      })


    CSVStream(stream)
  }

  def fromKafkaStream(kafkaStream: KafkaStream)(implicit env: StreamExecutionEnvironment): CSVStream = {
    // var's set up
    val defaultConfig = DefaultCSVConfig()
    val csvConfig = CustomCSVConfig(defaultConfig.delimiter, defaultConfig.quoteCharacter, "\n\n")


    // CSVFormat set up with delimiter and quote character
    val format = CSVFormat.newFormat(csvConfig.delimiter)
      .withQuote(csvConfig.quoteCharacter)
      .withTrim()
      .withFirstRecordAsHeader()

    val properties = kafkaStream.getProperties
    val consumer =  UniversalKafkaConnectorFactory.getSource(kafkaStream.topic, new SimpleStringSchema(), properties)
    val stream: DataStream[Iterable[Item]] = StreamUtil.paralleliseOverSlots(env.addSource(consumer))
      .map(batchString => {
        CSVItem.fromDataBatch(batchString, format)
      })
    CSVStream(stream)
  }

  def fromFileStream(path: String)(implicit senv: StreamExecutionEnvironment): CSVStream = {

//    implicit val tEnv: StreamTableEnvironment = StreamTableEnvironment.create(senv);
//
//    // standard delimiter //TODO: from RML mapping
//    val delimiter = ','
//    val quoteCharacter = '"'
//
//
//    val format = CSVFormat.newFormat(delimiter).withQuote(quoteCharacter).withTrim()
//    // extract header
//    val header: Option[Array[String]] = CSVHeader(Paths.get(path), format)
//
//    val csvDataStr = senv.readFile(new CSVInputFormat(path, CSVFormat.DEFAULT), path, FileProcessingMode.PROCESS_ONCE, 0)
//
//    // create the table
//    val table: Table = tEnv.fromDataStream(csvDataStr)
//
//    // create the header->index map
//    val headersMap = convertToIndexMap(header.get)
//
//    // convert to a Flink datastream for further processing
//    implicit val typeInfo = TypeInformation.of(classOf[Row])
//    implicit val rowItemTypeInfo = TypeInformation.of(classOf[Item])
//    val dataSet: DataStream[Item] = tEnv.toAppendStream(table)(typeInfo).map(row => row
//      .asInstanceOf[Item]) // needed since types of datastreams can't be subclasses due to Flink implementation
//
//    // create the CSV Stream
//    new CSVStream(dataSet)
    throw new NotImplementedError("FileStream is not implemented properly yet ")
  }

  private def convertToSelection(headers: Array[String]): String = {
    headers.reduce((a, b) => a + ", " + b)
  }

  private def convertToIndexMap(headers: Array[String]): Map[String, Int] = {
    var index = -1 // start will be 0
    headers.map(header => {
      index += 1
      (header, index)
    }).toMap
  }

}
