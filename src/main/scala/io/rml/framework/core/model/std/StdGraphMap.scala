package io.rml.framework.core.model.std

import io.rml.framework.core.model._
import io.rml.framework.core.vocabulary.RMLVoc

case class StdGraphMap(identifier: String,
                       constant: Option[Entity],
                       reference: Option[Literal],
                       template: Option[Literal]) extends GraphMap {

  override def termType: Option[Uri] = Some(Uri(RMLVoc.Class.IRI))

}
