package io.rml.framework.core.extractors

import io.rml.framework.core.extractors.std.{StdJoinConfigMapExtractor, TermMapExtractor}
import io.rml.framework.core.model.JoinConfigMap
import io.rml.framework.engine.composers.JoinType
import io.rml.framework.engine.windows.WindowType

trait JoinConfigMapExtractor extends  TermMapExtractor[Option[JoinConfigMap]]{
  val windowType:Option[WindowType]

}


object JoinConfigMapExtractor {

    def apply(windowType: Option[WindowType]): JoinConfigMapExtractor = {
         StdJoinConfigMapExtractor(windowType)
    }
}
