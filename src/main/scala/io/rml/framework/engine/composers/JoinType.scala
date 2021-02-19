package io.rml.framework.engine.composers

import io.rml.framework.core.vocabulary.RMLVoc

sealed trait JoinType

case object TumblingJoin extends JoinType
case object CrossJoin extends JoinType
case object VC_TWindowJoin extends JoinType


object JoinType {
  def fromUri(uri:String) : Option[JoinType] = {
    uri match{
      case RMLVoc.Class.TUMBLING_JOIN_TYPE => Some(TumblingJoin)
      case RMLVoc.Class.CROSS_JOIN_TYPE => Some(CrossJoin)
      case RMLVoc.Class.VC_TW_JOIN_TYPE =>Some(VC_TWindowJoin)
      case _ => None
    }
  }
  def fromString(value: String): Option[JoinType] = {
    Vector(TumblingJoin, CrossJoin, VC_TWindowJoin).find(_.toString == value)
  }
}
