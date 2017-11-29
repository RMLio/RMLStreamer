package io.rml.framework.flink.item

import org.apache.flink.types.Row

class RowItem(row: Row, headers: Map[String, Int]) extends Item {

  override def refer(reference: String) = {
    val index = headers(reference)
    val value = row.getField(index).toString
    if(value.nonEmpty) Some(value) else None
  }

  override def toString: String = {
    row.toString
  }

}

object RowItem {
  def apply(row: Row, headers: Map[String, Int]): RowItem = new RowItem(row, headers)
}
