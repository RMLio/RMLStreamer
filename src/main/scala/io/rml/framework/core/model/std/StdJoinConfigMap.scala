package io.rml.framework.core.model.std

import io.rml.framework.core.model.JoinConfigMap
import io.rml.framework.engine.composers.JoinType
import io.rml.framework.engine.windows.WindowType

case class StdJoinConfigMap(joinType:JoinType, windowType: WindowType)  extends  JoinConfigMap{

}
