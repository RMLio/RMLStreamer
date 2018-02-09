package io.rml.framework.flink.item

case class JoinedItems(items: List[Item]) {
  def apply(index: Int): Item = items(index)
}
