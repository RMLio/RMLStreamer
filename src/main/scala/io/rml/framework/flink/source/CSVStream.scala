package io.rml.framework.flink.source

import java.nio.file.Paths
import java.util.Properties

import io.rml.framework.core.model.KafkaStream
import io.rml.framework.flink.item.{Item, RowItem}
import io.rml.framework.flink.item.csv.{CSVHeader, CSVItem}
import io.rml.framework.flink.item.json.JSONItem
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.table.api.{Table, TableEnvironment, Types}
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.sources.CsvTableSource
import org.apache.flink.types.Row

case class CSVStream(stream: DataStream[Item], headers: Array[String]) extends Stream

object CSVStream {

  def fromTCPSocketStream(hostName: String, port: Int, headers: Array[String])(implicit env: StreamExecutionEnvironment) : CSVStream = {
    val delimiter = ','
    val stream = env.socketTextStream(hostName, port)
                    .flatMap(item => CSVItem(item, delimiter, headers))
                    .map(item => item.asInstanceOf[Item])
    CSVStream(stream, headers)
  }

  def fromKafkaStream(kafkaStream: KafkaStream, headers: Array[String])(implicit env: StreamExecutionEnvironment) : CSVStream = {
    val delimiter = ','
    val properties = new Properties()
    val brokersCommaSeparated = kafkaStream.brokers.reduce((a,b) => a + ", " + b)
    properties.setProperty("bootstrap.servers", brokersCommaSeparated)
    val zookeepersCommaSeparated = kafkaStream.zookeepers.reduce((a,b) => a + ", " + b)
    properties.setProperty("zookeepers.connect", zookeepersCommaSeparated)
    properties.setProperty("group.id", kafkaStream.groupId)
    val stream: DataStream[Item] = env.addSource(new FlinkKafkaConsumer08[String](kafkaStream.topic, new SimpleStringSchema(), properties))
                                      .map(item => CSVItem(item, delimiter, headers).asInstanceOf[Item])
    CSVStream(stream, headers)
  }

  def fromFileStream(path: String)(implicit senv: StreamExecutionEnvironment) : CSVStream = {

    implicit val tEnv: StreamTableEnvironment = TableEnvironment.getTableEnvironment(senv)

    // standard delimiter //TODO: from RML mapping
    val delimiter = ","

    // extract header
    val header: Option[Array[String]] = CSVHeader(Paths.get(path),delimiter.charAt(0))

    // create table source, tables are use for dynamically assigning headers
    val source = CsvTableSource.builder()
                               .path(path.replaceFirst("file://", ""))
                               .ignoreFirstLine() // skip the header
                               .fieldDelimiter(delimiter)

    // assign headers dynamically
    val builder = header.get.foldLeft(source)((a,b) => a.field(b, Types.STRING)).build()

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
    val dataSet: DataStream[Item] = tEnv.toAppendStream(table)(typeInfo).map(row => RowItem(row, headersMap)
      .asInstanceOf[Item]) // needed since types of datastreams can't be subclasses due to Flink implementation

    // create the CSV Stream
    new CSVStream(dataSet, headersMap.keySet.toArray)
  }

  private def convertToSelection(headers: Array[String]): String = {
    headers.reduce((a,b) => a + ", " + b)
  }

  private def convertToIndexMap(headers: Array[String]): Map[String, Int] = {
    var index = -1 // start will be 0
    headers.map(header => {
      index += 1
      (header, index)
    }).toMap
  }

}
