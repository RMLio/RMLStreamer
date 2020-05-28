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
package io.rml.framework

import java.io.File

import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.{FormattedRMLMapping, RMLMapping}
import io.rml.framework.engine.NopPostProcessor
import io.rml.framework.util.logging.Logger
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object Test extends App {

  implicit val env = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val postProcessor = new NopPostProcessor()
  val classLoader = getClass.getClassLoader
  val file = new File(classLoader.getResource("rml-testcases/RMLTC0007d-CSV/mapping.ttl").getFile)
  val mapping = MappingReader().read(file)

  val formattedMapping = FormattedRMLMapping.fromRMLMapping(mapping.asInstanceOf[RMLMapping])
  Logger.logInfo("" + formattedMapping.standardStaticTriplesMaps.size)
  val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect().reduce((a, b) => a + "\n" + b)
  System.out.println(result)
}
