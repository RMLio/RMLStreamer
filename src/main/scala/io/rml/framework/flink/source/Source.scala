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

import io.rml.framework.core.model.{FileDataSource, LogicalSource, StreamDataSource, Uri}
import io.rml.framework.core.vocabulary.QueryVoc
import io.rml.framework.shared.RMLException
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

trait Source

/**
  * Object for generating Flink Sources from a LogicalSource
  */
object Source {

  val DEFAULT_ITERATOR_MAP: Map[String, String] =  Map(
    QueryVoc.Class.JSONPATH -> "$",
    QueryVoc.Class.CSV -> "",
    QueryVoc.Class.XPATH -> "/*"
  )

  val DEFAULT_ITERATOR_SET: Set[String] = DEFAULT_ITERATOR_MAP.values.toSet

  def apply(logicalSource: LogicalSource)(implicit env: ExecutionEnvironment, senv: StreamExecutionEnvironment): Source = {
    logicalSource.source match {
      case _: FileDataSource => FileDataSet(logicalSource)
      case _: StreamDataSource => {
        logicalSource.source match {
          case source: StreamDataSource =>
            logicalSource.referenceFormulation match {
              case Uri(QueryVoc.Class.CSV) => CSVStream(logicalSource)
              case Uri(QueryVoc.Class.XPATH) => XMLStream(source, logicalSource.iterators.distinct)
              case Uri(QueryVoc.Class.JSONPATH) => JSONStream(source, logicalSource.iterators.distinct)
              case _ => throw new RMLException(s"Reference formulation of the resource unknown. Provided formulation: ${logicalSource.referenceFormulation}")
            }
        }
      }
    }
  }
}
