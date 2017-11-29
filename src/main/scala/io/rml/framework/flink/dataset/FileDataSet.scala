package io.rml.framework.flink.dataset

import com.alexholmes.json.mapreduce.MultiLineJsonInputFormat
import io.rml.framework._
import io.rml.framework.core.model.{LogicalSource, Uri}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.json.JSONItem
import io.rml.framework.flink.item.xml.XMLItem
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala._
import org.apache.flink.hadoopcompatibility.scala.HadoopInputs
import org.apache.flink.table.api.TableEnvironment
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.Job
import org.apache.mahout.text.wikipedia.XmlInputFormat

abstract class FileDataSet {
  def dataset : DataSet[Item]
}

object FileDataSet {

  def apply(logicalSource: LogicalSource)(implicit env: ExecutionEnvironment): FileDataSet = {

    logicalSource.referenceFormulation match {
      case Uri(RMLVoc.Class.CSV) => createCSVDataSet(logicalSource.source.uri.toString)
      case Uri(RMLVoc.Class.XPATH) => createXMLDataSet(logicalSource.source.uri.toString, logicalSource.iterator.get.value)
      case Uri(RMLVoc.Class.JSONPATH) => createJSONDataSet(logicalSource.source.uri.toString, logicalSource.iterator.get.value)
    }

  }

  private def createCSVDataSet(path: String)(implicit env: ExecutionEnvironment) : CSVDataSet = {
    implicit val tEnv = TableEnvironment.getTableEnvironment(env)
    val delimiter = ","
    val csvDataSet: CSVDataSet = CSVDataSet("airplanes", path, delimiter)
    csvDataSet
  }

  private def createXMLDataSet(path: String, tag: String)(implicit env: ExecutionEnvironment) : XMLDataSet = {
    println("Creating XMLDataSet from " + path + ", with tag " + tag)
    implicit val longWritableTypeInfo: TypeInformation[LongWritable] = TypeInformation.of(classOf[LongWritable])
    implicit val textTypeInfo: TypeInformation[Text] = TypeInformation.of(classOf[Text])
    val hInput = HadoopInputs.readHadoopFile(new XmlInputFormat(),classOf[LongWritable], classOf[Text], path)
    hInput.getConfiguration.set(XmlInputFormat.START_TAG_KEY, "<" + tag + ">")
    hInput.getConfiguration.set(XmlInputFormat.END_TAG_KEY, "</" + tag + ">")
    val hDataset = env.createInput(hInput)
    hDataset.print()
    val dataset: DataSet[Item] = hDataset.map(item => XMLItem.fromString(item._2.toString).asInstanceOf[Item])  // needed since types of datasets can't be subclasses due to Flink implementation
    XMLDataSet(dataset)
  }

  private def createJSONDataSet(path: String, tag: String)(implicit env: ExecutionEnvironment) : JSONDataSet = {
    println("Creating JSONDataSet from " + path + ", with tag " + tag)
    implicit val longWritableTypeInfo: TypeInformation[LongWritable] = TypeInformation.of(classOf[LongWritable])
    implicit val textTypeInfo: TypeInformation[Text] = TypeInformation.of(classOf[Text])
    val job = Job.getInstance()
    val hInput = HadoopInputs.readHadoopFile(new MultiLineJsonInputFormat(),classOf[LongWritable], classOf[Text], path, job)
    //hInput.getConfiguration.set("multilinejsoninputformat.member", tag)
    MultiLineJsonInputFormat.setInputJsonMember(job, tag)
    val hDataset = env.createInput(hInput)
    hDataset.print()
    val dataset: DataSet[Item] = hDataset.map(item => JSONItem.fromString(item._2.toString).asInstanceOf[Item])  // needed since types of datasets can't be subclasses due to Flink implementation
    JSONDataSet(dataset)
  }



}
