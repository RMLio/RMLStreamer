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
package io.rml.framework.util.server

import io.rml.framework.util.logging.Logger
import org.apache.flink.streaming.api.functions.sink.SinkFunction

/**
  * @author Gerald Haesendonck
  */

object TestSink2 extends SinkFunction[String] {
  private var triples: List[String] = List[String]()

  def apply(): TestSink2 = {
    triples = List[String]()
    new TestSink2()
  }

  def getTriples(): List[String] = {
    triples
  }
}

class TestSink2 extends SinkFunction[String] {
  import TestSink2._

  override def invoke(value: String, context: SinkFunction.Context[_]): Unit = {
    Logger.logInfo(s"TestSink2: got value [${value}]")
    if (value.trim.nonEmpty) {
      triples = value :: triples
    }
  }
}
