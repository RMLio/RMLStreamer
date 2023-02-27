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
 * */

package io.rml.framework.core.item.csv

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.item.Item
import org.apache.commons.csv.{CSVFormat, CSVRecord}

import java.io.StringReader
import scala.collection.JavaConverters._

/**
 *
 * @param record
 */
class CSVItem(record: Map[String, String], val tag: String, dataTypes: Map[String, String]) extends Item {

  /**
   *
   * @param reference
   * @return
   */
  override def refer(reference: String): Option[List[String]] = {
    val value = record.get(reference)
    if (value.isDefined) {
      if (value.get == null || value.get.isEmpty ) {
        None
      } else {
        Some(List(value.get))
      }
    } else {
      CSVItem.logWarning(s"Cannot find reference: '$reference'. Continuing with no value for this reference.")
      None
    }
  }

  override def getDataTypes: Map[String, String] = dataTypes
}


object CSVItem extends Logging {


  def apply(record: CSVRecord): CSVItem = {
    val recordMap = record.toMap
    new CSVItem(recordMap.asScala.toMap, "", Map.empty)
  }

  def fromDataBatch(dataBatch: String, csvFormat: CSVFormat): List[Item] = {

    //jdata batch string must not contain any leading whitespaces
    val sanitizedData = dataBatch.replaceAll("^\\s+", "").replace("\n\n", "")
    val parser = csvFormat.parse(new StringReader(sanitizedData))
    val result: List[Item] = parser.getRecords.asScala.toList.map(record =>
      new CSVItem(record.toMap.asScala.toMap, "", Map.empty)
    )
    result
  }

}
