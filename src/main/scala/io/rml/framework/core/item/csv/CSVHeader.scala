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

package io.rml.framework.core.item.csv

import io.rml.framework.flink.util.IOUtils
import org.apache.commons.csv.CSVFormat

import java.io.{IOException, StringReader}
import java.nio.file.Path
import scala.collection.JavaConverters._

object CSVHeader {

  def apply(path: Path, csvFormat: CSVFormat): Option[Array[String]] = {

    val line = IOUtils.readFirstLineFromUTF8FileWithBOM(path)
    getCSVHeaders(line, csvFormat)
  }

  def apply(csvData: String, csvFormat: CSVFormat, isBatch: Boolean = false): Option[Array[String]] = {
    if (isBatch) {
      val firstLine = csvData.split(csvFormat.getDelimiter)

      if (firstLine.isEmpty) None else getCSVHeaders(firstLine(0), csvFormat)
    } else {
      getCSVHeaders(csvData, csvFormat)

    }
  }

  private def getCSVHeaders(csvLine: String, csvFormat: CSVFormat): Option[Array[String]] = {
    try {
      val reader = new StringReader(csvLine)
      val parser = csvFormat
        .parse(reader)
      Some(parser.getRecords.get(0).iterator().asScala.toArray)
    } catch {
      case e: IOException => None
      case e: IndexOutOfBoundsException => None
    }

  }
}


