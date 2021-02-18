package io.rml.framework.engine.windows

sealed trait WindowType

case object TumblingWindow extends  WindowType
case object SlidingWindow extends  WindowType
case object VCTWindow extends  WindowType


object WindowType {
  def fromString(value: String): Option[WindowType] = {
    Vector(TumblingWindow, SlidingWindow, VCTWindow).find(_.toString == value)
  }
}