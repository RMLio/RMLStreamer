package io.rml.framework.core.extractors

import io.rml.framework.core.extractors.std.StdJoinConditionExtractor
import io.rml.framework.core.model.JoinCondition

trait JoinConditionExtractor extends ResourceExtractor[Option[JoinCondition]] {

}

object JoinConditionExtractor {
  def apply(): JoinConditionExtractor = new StdJoinConditionExtractor()
}
