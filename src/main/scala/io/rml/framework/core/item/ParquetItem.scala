package io.rml.framework.core.item

import org.apache.parquet.example.data.Group
import org.apache.parquet.schema.Type

class ParquetItem(group: Group, fields: List[Type]) extends Item {
  /**
   * Fetches the value of a column in the group
   * @param reference name of the column in the group
   * @return
   */
  override def refer(reference: String): Option[List[String]] = {
    // index is the index of the object in a certain group. We use 0 here, since there's only ever one object in a group
    val value = group.getString(reference, 0)
    if (value == null) Some(List(value)) else None
  }

  override def tag : String = ""
}
