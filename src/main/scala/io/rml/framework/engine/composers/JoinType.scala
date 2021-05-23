package io.rml.framework.engine.composers

import io.rml.framework.core.vocabulary.RMLVoc

sealed trait JoinType

case object TumblingJoin extends JoinType
case object CrossJoin extends JoinType
case object DynamicJoin extends JoinType


object JoinType {
  def fromUri(uri:String) : Option[JoinType] = {
    uri match{
      case RMLVoc.Class.TUMBLING_JOIN_TYPE => Some(TumblingJoin)
      case RMLVoc.Class.CROSS_JOIN_TYPE => Some(CrossJoin)
      case RMLVoc.Class.DYNAMIC_JOIN_TYPE =>Some(DynamicJoin)
      case _ => None
    }
  }
  def fromString(value: String): Option[JoinType] = {
    Vector(TumblingJoin, CrossJoin, DynamicJoin).find(_.toString == value)
  }
}
