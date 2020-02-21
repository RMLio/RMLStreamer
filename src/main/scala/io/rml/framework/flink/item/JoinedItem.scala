package io.rml.framework.flink.item

case class JoinedItem(child: Item, parent: Item) extends  Item {
  override def refer(reference: String): Option[List[String]] = {
    throw new IllegalAccessError("Joined item cannot call refer!")
  }

  override def tag: String = ""
}
