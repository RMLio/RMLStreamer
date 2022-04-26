package io.rml.framework.engine.windows

import io.rml.framework.core.vocabulary.RMLSVoc

sealed trait WindowType

case object TumblingWindow extends WindowType

case object SlidingWindow extends WindowType

case object DynamicWindow extends WindowType


object WindowType {
  private val SUB_TYPE_LIST = Vector(
    TumblingWindow,
    SlidingWindow,
    DynamicWindow)

  def fromUri(uri: String): Option[WindowType] = {
    uri match {
      case RMLSVoc.Class.DYNAMIC_WINDOW => Some(DynamicWindow)
      case RMLSVoc.Class.TUMBLING_WINDOW => Some(TumblingWindow)
      case RMLSVoc.Class.SLIDING_WINDOW => Some(SlidingWindow)
      case _ => None
    }
  }

  def fromString(value: String): Option[WindowType] = {
    SUB_TYPE_LIST.find(_.toString == value)
  }
}