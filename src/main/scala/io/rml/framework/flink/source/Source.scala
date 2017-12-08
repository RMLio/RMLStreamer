package io.rml.framework.flink.source

import io.rml.framework.core.model.{FileDataSource, LogicalSource, StreamDataSource}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

trait Source

object Source {

  def apply(logicalSource: LogicalSource)(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment): Source = {
    logicalSource.source match {
      case fs: FileDataSource => FileDataSet(logicalSource)
      case ss: StreamDataSource => StreamDataSource.fromLogicalSource(logicalSource)
    }
  }

}
