package io.rml.framework.flink.dataset

import java.nio.file.Paths

import io.rml.framework.flink.item.{Item, RowItem}
import io.rml.framework.flink.item.csv.CSVHeader
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala.DataSet
import org.apache.flink.table.api.scala.BatchTableEnvironment
import org.apache.flink.table.api.{Table, Types}
import org.apache.flink.table.sources.CsvTableSource
import org.apache.flink.types.Row

case class CSVDataSet(dataset: DataSet[Item], headers: Map[String, Int]) extends FileDataSet

object CSVDataSet {

  def apply(name: String, path: String, delimiter: String)(implicit tEnv: BatchTableEnvironment): CSVDataSet = {

    // extract header
    val header: Option[Array[String]] = CSVHeader(Paths.get(path),delimiter.charAt(0))

    // create table source, tables are use for dynamically assigning headers
    val airplaneSource = CsvTableSource.builder()
      .path(path.replaceFirst("file://", ""))
      .ignoreFirstLine() // skip the header
      .fieldDelimiter(delimiter)

    // assign headers dynamically
    val builder = header.get.foldLeft(airplaneSource)((a,b) => a.field(b, Types.STRING)).build()

    // register the table to the table environment
    tEnv.registerTableSource(name, builder)

    // create the table
    val table: Table = tEnv
      .scan(name)
      .select(convertToSelection(header.get))

    // create the header->index map
    val headersMap = convertToIndexMap(header.get)

    // convert to a Flink dataset for further processing
    implicit val typeInfo = TypeInformation.of(classOf[Row])
    implicit val rowItemTypeInfo = TypeInformation.of(classOf[Item])
    val dataSet: DataSet[Item] = tEnv.toDataSet(table)(typeInfo).map(row => RowItem(row, headersMap)
      .asInstanceOf[Item]) // needed since types of datasets can't be subclasses due to Flink implementation

    // create the CSV DataSet
    new CSVDataSet(dataSet, headersMap)

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
