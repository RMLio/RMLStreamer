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

import io.rml.framework.core.item.csv.CSVItem
import io.rml.framework.core.item.{EmptyItem, Item}
import org.apache.commons.csv.{CSVFormat, CSVParser, CSVRecord}
import org.apache.flink.api.common.io.GenericCsvInputFormat
import org.apache.flink.core.fs.Path


/**
  * Extending GenericCsvInputFormat allows us to use CSV parser from apache commons instead of the broken parser from flink.
  *
  */
class CSVInputFormat(filePath: String,csvFormat: CSVFormat) extends GenericCsvInputFormat[Item](new Path(filePath)) {

    if(csvFormat == null){
      throw new IllegalArgumentException(s"CSVFormat provided to $this cannot be null")
    }
  {
    val header: Array[String] = csvFormat.getHeader
    if (header.isEmpty) {
      throw new IllegalArgumentException("Header for the CSV Input needs to be provided! header.size: " + header.length)
    }

    setSkipFirstLineAsHeader(true)
  }

  override def readRecord(reuse: Item, bytes: Array[Byte], offset: Int, numBytes: Int): Item = {

    val line = bytes.slice(offset, numBytes + offset).map(_.toChar).mkString("")

    val csvIter: java.util.Iterator[CSVRecord] = CSVParser.parse(line, csvFormat).iterator()

    if (csvIter.hasNext) {
      val csvRecord = csvIter.next();
      CSVItem(csvRecord)

    } else {

      new EmptyItem

    }
  }

}
