package io.rml.framework.flink.item.csv

import org.apache.flink.types.Row

class CSVRowItem(row: Row, headers: Map[String, Int]) extends CSVItem {

  override def refer(reference: String) = {
    val index = headers.get(reference)
    if (index.nonEmpty) Some(row.getField(index.get).toString)
    else None
  }

  override def toString: String = {
    row.toString
  }

}

object CSVRowItem {
  def apply(row: Row, headers: Map[String, Int]): CSVRowItem = new CSVRowItem(row, headers)
}
