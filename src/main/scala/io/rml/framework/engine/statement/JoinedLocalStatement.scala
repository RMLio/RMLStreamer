package io.rml.framework.engine.statement

import io.rml.framework.flink.item.{Item, JoinedItem, JoinedItems}
import io.rml.framework.flink.sink.FlinkRDFTriple
/**
class JoinedLocalStatement(index: Int, statement: Statement[Item]) extends Statement[JoinedItems]{
  override def process(items: JoinedItems): Option[FlinkRDFTriple] = {
    val currentItem = items(index)
    val localTriple = statement.process(currentItem)
    localTriple
  }
}

class JoinedChildStatement(index: Int, statement: ChildStatement[Item]) extends Statement[JoinedItems]{
  override def process(items: JoinedItems): Option[FlinkRDFTriple] = {
    val currentItem = items(index)
    val parentItem = items(index)
    val triple = statement.process(JoinedItem(currentItem, parentItem))
    triple
  }
}
**/