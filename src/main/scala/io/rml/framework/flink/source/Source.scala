package io.rml.framework.flink.source

import io.rml.framework.core.model.{FileDataSource, LogicalSource, StreamDataSource}
import io.rml.framework.core.vocabulary.RMLVoc
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

trait Source

/**
  * Object for generating Flink Sources from a LogicalSource
  */
object Source {

  val DEFAULT_ITERATOR_MAP: Map[String, Option[String]] =  Map(
    RMLVoc.Class.JSONPATH -> Some("$"),
    RMLVoc.Class.CSV -> None,
    RMLVoc.Class.XPATH -> Some("/*")
  )

  val DEFAULT_ITERATOR_SET: Set[String] = DEFAULT_ITERATOR_MAP.values.flatten.toSet

  def apply(logicalSource: LogicalSource)(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment): Source = {
    logicalSource.source match {
      case fs: FileDataSource => FileDataSet(logicalSource)
      case ss: StreamDataSource => StreamDataSource.fromLogicalSource(logicalSource)
    }
  }

}
