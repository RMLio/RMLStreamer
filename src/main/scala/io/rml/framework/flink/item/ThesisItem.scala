package io.rml.framework.flink.item

import io.rml.framework.core.vocabulary.RMLVoc

case class ThesisItem(item: Item, time:Long) extends  Item {
  override def refer(reference: String): Option[List[String]] = {
    if (reference == "latencyRef") {
      return Some(List(time.toString))
    }else{
      item.refer(reference)
    }
  }

  override def tag: String = ""
}
