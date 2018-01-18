package io.rml.framework.core.extractors

import io.rml.framework.core.extractors.std.TermMapExtractor
import io.rml.framework.core.model.GraphMap

trait GraphMapExtractor extends TermMapExtractor[Option[GraphMap]]
