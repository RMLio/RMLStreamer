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

import io.rml.framework.core.internal.Logging
import io.rml.framework.flink.item.Item
import org.apache.commons.csv.{CSVFormat, CSVRecord}
import org.apache.commons.io.IOUtils

/**
  *
  * @param record
  */
class CSVItem(record: CSVRecord) extends Item {

  /**
    *
    * @param reference
    * @return
    */
  override def refer(reference: String): Option[List[String]] = {
    try {
      Some(List(record.get(reference)))
    } catch {
      case ex: IllegalArgumentException => {
        println(ex)
        None
      }
    }
  }

}


object CSVItem {


  def apply(record: CSVRecord): CSVItem = new CSVItem(record)


  //TODO remove this method and use the one with CSVFormat
  def apply(csvLine: String, delimiter: Char, quoteCharacter: Char, headers: Array[String]): Option[CSVItem] = {
    CSVItem(csvLine, CSVFormat.newFormat(delimiter)
      .withQuote(quoteCharacter)
      .withHeader(headers: _*) // convert to Java var args
      .withTrim())
  }

  def apply(csvLine: String, cSVFormat: CSVFormat): Option[CSVItem] = {
    try {
      val in = IOUtils.toInputStream(csvLine, "UTF-8")
      val reader = new InputStreamReader(in, "UTF-8")
      val record = cSVFormat
        .parse(reader)
        .getRecords.get(0)
      Some(CSVItem(record))
    } catch {
      case e: IOException => None
      case e: IndexOutOfBoundsException => None
    }


  }

}
