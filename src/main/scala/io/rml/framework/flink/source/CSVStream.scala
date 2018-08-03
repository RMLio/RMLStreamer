package io.rml.framework.flink.source

import java.nio.file.Paths
import java.util.Properties

import io.rml.framework.core.model.{FileStream, KafkaStream, StreamDataSource, TCPSocketStream}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.csv.{CSVHeader, CSVItem}
import io.rml.framework.flink.util.{CustomCSVConfig, DefaultCSVConfig}
import org.apache.commons.csv.CSVFormat
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.api.{Table, TableEnvironment, Types}
import org.apache.flink.table.sources.CsvTableSource
import org.apache.flink.types.Row

case class CSVStream(stream: DataStream[Item], headers: Array[String]) extends Stream

object CSVStream {

  def apply(source: StreamDataSource)(implicit env: StreamExecutionEnvironment): Stream = {

    source match {
      case tcpStream: TCPSocketStream => fromTCPSocketStream(tcpStream)
      case fileStream: FileStream => fromFileStream(fileStream.path)
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

    val stream = StreamUtil.createTcpSocketSource(tCPSocketStream, csvConfig.recordDelimiter)
      .flatMap(batchString => {
        CSVItem.fromDataBatch(batchString, format)
      })
        .flatMap(item => item)
    CSVStream(stream, Array.empty)
  }

  def fromKafkaStream(kafkaStream: KafkaStream, headers: Array[String])(implicit env: StreamExecutionEnvironment): CSVStream = {
    val delimiter = ','
    val quoteCharacter = '"'
    val properties = new Properties()
    val brokersCommaSeparated = kafkaStream.brokers.reduce((a, b) => a + ", " + b)
    properties.setProperty("bootstrap.servers", brokersCommaSeparated)
    val zookeepersCommaSeparated = kafkaStream.zookeepers.reduce((a, b) => a + ", " + b)
    properties.setProperty("zookeepers.connect", zookeepersCommaSeparated)
    properties.setProperty("group.id", kafkaStream.groupId)
    val stream: DataStream[Item] = env.addSource(new FlinkKafkaConsumer08[String](kafkaStream.topic, new SimpleStringSchema(), properties))
      .map(item => CSVItem(item, delimiter, quoteCharacter, headers).asInstanceOf[Item])
    CSVStream(stream, headers)
  }

  def fromFileStream(path: String)(implicit senv: StreamExecutionEnvironment): CSVStream = {

    implicit val tEnv: StreamTableEnvironment = TableEnvironment.getTableEnvironment(senv)

    // standard delimiter //TODO: from RML mapping
    val delimiter = ','
    val quoteCharacter = '"'


    val format = CSVFormat.newFormat(delimiter).withQuote(quoteCharacter).withTrim()
    // extract header
    val header: Option[Array[String]] = CSVHeader(Paths.get(path), format)

    // create table source, tables are use for dynamically assigning headers
    val source = CsvTableSource.builder()
      .path(path.replaceFirst("file://", ""))
      .ignoreFirstLine() // skip the header
      .fieldDelimiter(delimiter.toString)

    // assign headers dynamically
    val builder = header.get.foldLeft(source)((a, b) => a.field(b, Types.STRING)).build()

    // register the table to the table environment
    tEnv.registerTableSource(path, builder)

    // create the table
    val table: Table = tEnv
      .scan(path)
      .select(convertToSelection(header.get))

    // create the header->index map
    val headersMap = convertToIndexMap(header.get)

    // convert to a Flink datastream for further processing
    implicit val typeInfo = TypeInformation.of(classOf[Row])
    implicit val rowItemTypeInfo = TypeInformation.of(classOf[Item])
    val dataSet: DataStream[Item] = tEnv.toAppendStream(table)(typeInfo).map(row => row
      .asInstanceOf[Item]) // needed since types of datastreams can't be subclasses due to Flink implementation

    // create the CSV Stream
    new CSVStream(dataSet, headersMap.keySet.toArray)
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
