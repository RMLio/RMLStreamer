package io.rml.framework.flink.source

import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.csv.CSVItem
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

      CSVItem(csvIter.next())

    } else {

      new EmptyItem

    }
  }

}
