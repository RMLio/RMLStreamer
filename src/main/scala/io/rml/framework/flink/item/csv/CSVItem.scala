package io.rml.framework.flink.item.csv

import io.rml.framework.flink.item.Item

trait CSVItem extends Item{
}

object CSVItem {
  private var _delimiter = ","
  var quoteCharacter = '"'


  //In preparation for implementation in CSVStream as batch delimiter
  private val batchDelimiter = "#"

  def delimiter: String = _delimiter

  def delimiter_=(delim: String): Unit = {
    if (delim == null || delim.length <= 0) {
      throw new IllegalStateException("The new delimiter cannot be null nor empty")

    }
    _delimiter = delim

  }
}
