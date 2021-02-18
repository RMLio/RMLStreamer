package io.rml.framework.core.model

import io.rml.framework.engine.composers.JoinType
import io.rml.framework.engine.windows.WindowType

trait JoinConfigMap {
   def joinType:JoinType
   def windowType: WindowType
}

object JoinConfigMap {

  def apply(joinType:JoinType, windowType: WindowType):JoinConfigMap => {
    StdConfigMap(joinType, windowType)
  }
}


