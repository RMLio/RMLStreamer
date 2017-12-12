package io.rml.framework.core.model.std

import io.rml.framework.core.model.{GraphMap, Literal, Uri, Value}
import io.rml.framework.core.vocabulary.RMLVoc

case class StdGraphMap( uri: Uri,
                        constant: Option[Value],
                        reference: Option[Literal],
                        template: Option[Literal],
                      ) extends GraphMap {

  override def termType: Option[Uri] = Some(Uri(RMLVoc.Class.IRI))

}
