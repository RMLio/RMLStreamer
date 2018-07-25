package io.rml.framework.flink.source

import java.io.File
import java.nio.charset.Charset

import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.csv.{CSVRowItem, CSVRecordItem}
import org.apache.commons.csv.{CSVFormat, CSVParser, CSVRecord}
import org.apache.flink.api.common.io.{GenericInputFormat, NonParallelInput}
import org.apache.flink.core.io.GenericInputSplit

class CSVInputFormat(filePath: String, delimiter: Char = ',', quoteChar: Char = '"') extends GenericInputFormat[Item] with NonParallelInput {

  private var csvIterator: java.util.Iterator[CSVRecord] = _

  override def open(split: GenericInputSplit): Unit = {
    super.open(split)
    val csvFile: File = new File(filePath)

    val format: CSVFormat = CSVFormat.newFormat(delimiter)
      .withQuote(quoteChar)
      .withTrim()
      .withFirstRecordAsHeader()


    csvIterator = CSVParser.parse(csvFile, Charset.forName("UTF-8"), format).iterator()

  }

  override def reachedEnd(): Boolean = !csvIterator.hasNext

  override def nextRecord(reuse: Item): Item = {
    val record = csvIterator.next()
    if(record != null) CSVRecordItem(record) else new EmptyItem

  }
}
