package io.rml.framework.flink.source

import io.rml.framework.flink.item.Item
import org.apache.flink.api.scala.DataSet

case class XMLDataSet(dataset: DataSet[Item]) extends FileDataSet

case class JSONDataSet(dataset: DataSet[Item]) extends FileDataSet