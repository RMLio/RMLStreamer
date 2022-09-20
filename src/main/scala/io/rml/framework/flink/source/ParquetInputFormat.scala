package io.rml.framework.flink.source

import io.rml.framework.core.item.Item
import org.apache.flink.api.common.io.GenericInputFormat

class ParquetInputFormat(path: String) extends GenericInputFormat[Item] {
  override def reachedEnd(): Boolean = ???

  override def nextRecord(ot: Item): Item = ???
}
