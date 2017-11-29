package io.rml.framework.flink

import io.rml.framework.core.model.{FileDataSource, LogicalSource, Uri}
import io.rml.framework.flink.dataset.FileDataSet
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}


object Source {

  def apply(logicalSource: LogicalSource)(implicit env: ExecutionEnvironment): FileDataSet = {
    logicalSource.source match {
      case fs: FileDataSource => FileDataSet(logicalSource)
    }
  }

}
