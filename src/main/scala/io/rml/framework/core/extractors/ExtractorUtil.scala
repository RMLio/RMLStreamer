package io.rml.framework.core.extractors

import io.rml.framework.core.model.Literal
import io.rml.framework.core.model.rdf.{RDFNode, RDFResource}
import io.rml.framework.shared.RMLException

object ExtractorUtil {

  def matchLiteral(node: RDFNode): Literal = {
    node match {
      case literal: Literal => literal
      case res: RDFResource => throw new RMLException(res.uri + ": must be a literal.")
    }
  }

}
