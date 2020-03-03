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
package io.rml.framework.util.fileprocessing

import java.io.File

import io.rml.framework.core.util.{Format, Util}
import io.rml.framework.util.CommentFilter

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * A test helper object to get strings from the expected output turtle file.
  *
  */
object ExpectedOutputTestUtil extends FileProcessingUtil[ (List[String], Option[Format]) ] {
  val filters = Array(new CommentFilter)



  /**
    * Read the given file and return a set of strings (output turtle triples are assumed to be distinct from each other)
    *
    * @param file output turtle file
    * @return set of unique triples in the output turtle file for comparison in the tests
    */
  override def processFile(file: File): (List[String], Option[Format]) = {
    val format = Util.guessFormatFromFileName(file.getName)

    var result: ListBuffer[String] = ListBuffer()

    for (line <- Source.fromFile(file).getLines()) {
      if (filters.map(_.check(line)).forall(p => p)) {
        result += line
      }
    }
    (result.toList, format)
  }

  override def candidateFiles: List[String] = List("output.ttl", "output.nq", "output.json")
}
