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
import io.rml.framework.core.model.{LogicalSource, Uri}
import io.rml.framework.core.vocabulary.QueryVoc
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.csv.CSVHeader
import io.rml.framework.flink.util.DefaultCSVConfig
import org.apache.commons.csv.CSVFormat
import org.apache.flink.api.scala._

import java.nio.file.Paths

sealed abstract class FileDataSet extends Source {
  def dataset: DataSet[Item]
}

case class XMLDataSet(dataset: DataSet[Item]) extends FileDataSet

case class JSONDataSet(dataset: DataSet[Item]) extends FileDataSet
case class CSVDataSet(dataset: DataSet[Item]) extends FileDataSet

/**
  * Object for creating Flink Datasets from a LogicalSource
  */
object FileDataSet extends Logging {

  def apply(logicalSource: LogicalSource)(implicit env: ExecutionEnvironment): FileDataSet = {
    logicalSource.referenceFormulation match {
      case Uri(QueryVoc.Class.CSV) => createCSVDataSet(logicalSource.source.uri.toString)
      case Uri(QueryVoc.Class.XPATH) => createXMLWithXPathDataSet(logicalSource.source.uri.toString, logicalSource.iterators.head)
      case Uri(QueryVoc.Class.JSONPATH) => createJSONWithJSONPathDataSet(logicalSource.source.uri.toString, logicalSource.iterators.head)
    }

  }

  def createCSVDataSet(path: String)(implicit env: ExecutionEnvironment): CSVDataSet = {
    val config = DefaultCSVConfig()
    val format =  CSVFormat.newFormat(config.delimiter)
      .withQuote(config.quoteCharacter)
      .withTrim()
    val header = CSVHeader(Paths.get(path), format).getOrElse(Array.empty)
    val dataset = env.createInput(new CSVInputFormat(path,format.withHeader(header:_*)))
    CSVDataSet(dataset)
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
