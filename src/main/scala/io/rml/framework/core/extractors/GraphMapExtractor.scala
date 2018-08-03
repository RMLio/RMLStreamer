package io.rml.framework.core.extractors

import io.rml.framework.core.extractors.std.{StdGraphMapExtractor, TermMapExtractor}
import io.rml.framework.core.model.GraphMap

trait GraphMapExtractor extends TermMapExtractor[Option[GraphMap]]


object GraphMapExtractor {
  def apply(): GraphMapExtractor = new StdGraphMapExtractor()
}