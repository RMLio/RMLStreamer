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

import scala.io.Source

object DataSourceTestUtil extends FileProcessingUtil[List[String]] {
  override def candidateFiles: List[String] = List[String]("datasource.xml", "datasource.json", "datasource.csv")


  override def processFile(file: File): List[String] = {

    var result = List[String]()
    var entry = ""
    //TODO: REFACTOR THIS UGLY QUICKIE WICKIE CODE FIX FOR CSV DATA READING
    val regex = "csv".r

    val csvMatches = regex.findAllMatchIn(file.getName)
    val tail = if (csvMatches.hasNext) "\n\n" else "\n"

    for (line <- Source.fromFile(file).getLines) {

      val trimmed = line.trim
      if (trimmed.length > 0) {
        if (trimmed.charAt(0) == '=') {
          // more than one data source entry detected in the file

          result ::= entry + tail
          entry = ""
        } else {
          var formattedLine = line.replaceAll(" +", " ")
          formattedLine = if (csvMatches.hasNext) formattedLine + "\n" else formattedLine
          entry += formattedLine
        }
      }
    }

    //append last entry in data source to the result list
    result ::= entry + tail

    result


  }
}
