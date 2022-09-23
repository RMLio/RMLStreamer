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
import io.rml.framework.core.item.csv.{CSVHeader, CSVItem}
import io.rml.framework.core.model.csvw.CSVWFileSource
import io.rml.framework.core.model.{LogicalSource, Uri}
import io.rml.framework.core.vocabulary.QueryVoc
import org.apache.commons.csv.CSVFormat
import org.apache.flink.api.scala._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.hadoop.util.HadoopInputFile
import org.apache.parquet.io.ColumnIOFactory

import java.nio.file.Paths
import scala.collection.JavaConverters._
import scala.collection.mutable


sealed abstract class FileDataSet extends Source {
  def dataset: DataSet[Item]
}

case class XMLDataSet(dataset: DataSet[Item]) extends FileDataSet
case class JSONDataSet(dataset: DataSet[Item]) extends FileDataSet
case class CSVDataSet(dataset: DataSet[Item]) extends FileDataSet
case class ParquetDataSet(dataset: DataSet[Item]) extends FileDataSet

/**
  * Object for creating Flink Datasets from a LogicalSource
  */
object FileDataSet extends Logging {

  def apply(logicalSource: LogicalSource)(implicit env: ExecutionEnvironment): FileDataSet = {
    logicalSource.referenceFormulation match {
      case Uri(QueryVoc.Class.CSV) => createCSVDataSet(logicalSource)
      case Uri(QueryVoc.Class.XPATH) => createXMLWithXPathDataSet(logicalSource.source.uri.value, logicalSource.iterators.head)
      case Uri(QueryVoc.Class.JSONPATH) => createJSONWithJSONPathDataSet(logicalSource.source.uri.value, logicalSource.iterators.head)
      case Uri(QueryVoc.Class.Parquet) => createParquetDataSet(logicalSource.source.uri.value)
    }
  }

  def createCSVDataSet(logicalSource: LogicalSource)(implicit env: ExecutionEnvironment): CSVDataSet = {
    logDebug("Creating CSV data set")
    var format = CSVFormat.Builder.create().setAllowMissingColumnNames(true)
    val path = logicalSource.source.uri.value

    logicalSource.source match {
      case csvw: CSVWFileSource =>
        format = csvw.dialect.setOptions(format)
        //val header = CSVHeader(Paths.get(path), format.build()).getOrElse(Array.empty)
        //val csvInputFormat = new CSVInputFormat(path)
        //val dataset = env.createInput(new CSVWInputFormat(path, header, csvw.dialect))
        //CSVDataSet(dataset)

      case _ => // a regular CSV file
        //val dataset = env.createInput(new CSVInputFormat(path))
        //CSVDataSet(dataset)
        //format.build()
    }

    val header = CSVHeader(Paths.get(path), format.build()).getOrElse(Array.empty)
    format.setHeader(header:_*)

    val csvInputFormat = new CSVInputFormat(path, format.build())
    val dataSet = env.createInput(csvInputFormat)
    CSVDataSet(dataSet)
  }

  /**
   * Reads the parquet file into a DataSet.
   * @param path path to the file
   * @param env execution environment
   * @return the read DataSet
   */
  def createParquetDataSet(path: String)(implicit env: ExecutionEnvironment): FileDataSet = {
//    var items = List[Item]()
//
//    // create a reader of the file and fetch information about the file
//    val reader = ParquetFileReader.open(HadoopInputFile.fromPath(new Path(path), new Configuration()))
//    val schema = reader.getFooter.getFileMetaData.getSchema
//    val fields = schema.getFields.asScala
//
//    try {
//      // read all of the pages
//      var pages = reader.readNextRowGroup() // throws RuntimeException when file is empty
//      while (pages != null) {
//        // each page consists of a number of rows
//        val rowCount = pages.getRowCount.toInt
//        // Parquet is column based, we therefore read the columns and convert the different rows to Groups
//        val columnIO = new ColumnIOFactory().getColumnIO(schema)
//        val recordReader = columnIO.getRecordReader(pages, new GroupRecordConverter(schema))
//
//        // every group will be converted to an instance of CSVItem
//        // this is done because of the way we parse the file: it becomes nothing more than a mapping
//        //    from header field to field value.
//        // This way, we can also leverage already existing operations instead of duplicating them.
//        for (_ <- 0 until rowCount) {
//          val group = recordReader.read()
//          // construct the Map for the CSVItem
//          val map = mutable.HashMap[String, String]()
//          for (i <- fields.indices) {
//            val value = group.getValueToString(i, 0)
//            map.put(fields(i).getName, value)
//          }
//          items = items ++ List(new CSVItem(map.toMap, ""))
//        }
//
//        pages = reader.readNextRowGroup()
//      }
//    } catch {
//      case _: RuntimeException =>
//        logWarning("Parsing an empty file!")
//        // nothing more needs to be done, Flink can handle empty dataset
//    }
//    reader.close()
//
//    ParquetDataSet(env.fromCollection(items))

    logDebug("Creating Parquet DataSet")
    val dataset = env.createInput(new ParquetInputFormat(path))
    ParquetDataSet(dataset)
  }

  /**
    * Not used
    *
    * @param path
    * @param xpath
    * @param env
    * @return
    */
  //@Deprecated
  /*def createXMLDataSet(path: String, tag: String)(implicit env: ExecutionEnvironment): XMLDataSet = {
    println("Creating XMLDataSet from " + path + ", with tag " + tag)
    implicit val longWritableTypeInfo: TypeInformation[LongWritable] = TypeInformation.of(classOf[LongWritable])
    implicit val textTypeInfo: TypeInformation[Text] = TypeInformation.of(classOf[Text])
    val hInput = HadoopInputs.readHadoopFile(new XmlInputFormat(), classOf[LongWritable], classOf[Text], path)
    hInput.getConfiguration.set(XmlInputFormat.START_TAG_KEY, "<" + tag.split(' ').head + ">")
    hInput.getConfiguration.set(XmlInputFormat.END_TAG_KEY, "</" + tag.split(' ').head + ">")
    val hDataset = env.createInput(hInput)
    val dataset: DataSet[Item] = hDataset.map(item => {
      XMLItem.fromString(item._2.toString).asInstanceOf[Item]
    }) // needed since types of datasets can't be subclasses due to Flink implementation
    XMLDataSet(dataset)
  }*/

  def createXMLWithXPathDataSet(path: String, xpath: String)(implicit env: ExecutionEnvironment): XMLDataSet = {
    logDebug("Creating XMLDataSet with XPath from " + path + ", with xpath " + xpath)
    val dataset = env.createInput(new XMLInputFormat(path, xpath))
    XMLDataSet(dataset)
  }

  def createJSONWithJSONPathDataSet(path: String, jsonPath: String)(implicit env: ExecutionEnvironment): JSONDataSet = {
    logDebug("Creating JSONDataSet from " + path + ", with JsonPath " + jsonPath)
    val dataset = env.createInput(new JSONInputFormat(path, jsonPath))
    JSONDataSet(dataset)
  }


}
