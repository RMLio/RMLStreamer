package io.rml.framework.core.extractors

import io.rml.framework.core.extractors.std.{StdJoinConfigMapExtractor, TermMapExtractor}
import io.rml.framework.core.model.JoinConfigMap
import io.rml.framework.engine.composers.JoinType

trait JoinConfigMapExtractor extends  TermMapExtractor[Option[JoinConfigMap]]{


}


object JoinConfigMapExtractor {

    def apply(): JoinConfigMapExtractor = {
         StdJoinConfigMapExtractor()
    }
}
