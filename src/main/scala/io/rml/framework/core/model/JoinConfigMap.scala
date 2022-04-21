package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdJoinConfigMap
import io.rml.framework.engine.composers.{CrossJoin, JoinType}
import io.rml.framework.engine.windows.WindowType

trait JoinConfigMap extends TermMap {
  def joinType: JoinType


  override  def logicalTargets: Set[LogicalTarget] = Set()

  def windowType: Option[WindowType]
}

object JoinConfigMap {
  // Default join config map for tumbling join
  def apply(joinCondition: Option[JoinCondition]): JoinConfigMap = {
    joinCondition match {
      case Some(_) => StdJoinConfigMap("")
      case None => StdJoinConfigMap("", CrossJoin)
    }
  }

  def apply(identifier: String, joinType: JoinType, windowType: Option[WindowType]): JoinConfigMap = {
    StdJoinConfigMap(identifier, joinType, windowType)
  }
}


