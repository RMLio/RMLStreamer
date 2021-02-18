package io.rml.framework.core.extractors

import io.rml.framework.core.extractors.std.TermMapExtractor
import io.rml.framework.core.model.JoinConfigMap
import io.rml.framework.engine.composers.JoinType

trait JoinConfigMapExtractor extends  TermMapExtractor[Option[JoinConfigMap]]{
    def joinType:JoinType
}


object JoinConfigMapExtractor {

  def apply():JoinConfigMap =>{

  }
}
