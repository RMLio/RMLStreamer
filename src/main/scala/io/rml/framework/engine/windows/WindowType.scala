package io.rml.framework.engine.windows

import io.rml.framework.core.vocabulary.RMLVoc

sealed trait WindowType

case object TumblingWindow extends WindowType

case object SlidingWindow extends WindowType

case object VCTWindow extends WindowType


object WindowType {
  private val SUB_TYPE_LIST = Vector(
    TumblingWindow,
    SlidingWindow,
    VCTWindow)

  def fromUri(uri: String): Option[WindowType] = {
    uri match {
      case RMLVoc.Class.VC_TWINDOW => Some(VCTWindow)
      case RMLVoc.Class.TUMBLING => Some(TumblingWindow)
      case RMLVoc.Class.SLIDING => Some(SlidingWindow)
      case _ => None
    }
  }

  def fromString(value: String): Option[WindowType] = {
    SUB_TYPE_LIST.find(_.toString == value)
  }
}