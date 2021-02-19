package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdJoinConfigMap
import io.rml.framework.engine.composers.JoinType
import io.rml.framework.engine.windows.WindowType

trait JoinConfigMap extends TermMap {
   def joinType:JoinType
   def windowType: Option[WindowType]
}

object JoinConfigMap {

  def apply(identifier:String, joinType:JoinType, windowType: Option[WindowType]):JoinConfigMap = {
    StdJoinConfigMap(identifier, joinType, windowType)
  }
}


