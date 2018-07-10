/*
 * Copyright (c) 2017 Ghent University - imec
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.rml.framework.flink.item.csv

import java.io.{IOException, InputStreamReader}
import java.nio.file.Path

import org.apache.commons.csv.CSVFormat
import org.apache.commons.io.IOUtils

import scala.collection.JavaConverters._
import scala.io.Source

object CSVHeader {

  def apply(path: Path, delimiter: Char): Option[Array[String]] = {
    val src = Source.fromFile(path.toString)
    val line = src.getLines.take(1).next()
    println(line)
    src.close
    CSVHeader(line, delimiter)
  }

  def apply(csvLine: String, delimiter: Char): Option[Array[String]] = {
    try {
      val in = IOUtils.toInputStream(csvLine, "UTF-8")
      val reader = new InputStreamReader(in, "UTF-8")
      val parser = CSVFormat.newFormat(delimiter)
        .withQuote('"')
        .withTrim()
        .parse(reader)

      Some(parser.getRecords.get(0).iterator().asScala.toArray)
    } catch {
      case e: IOException => None
      case e: IndexOutOfBoundsException => None
    }

  }
}


