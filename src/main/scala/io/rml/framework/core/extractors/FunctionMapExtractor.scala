package io.rml.framework.core.extractors

import io.rml.framework.core.model.rdf.{RDFLiteral, RDFResource}
import io.rml.framework.core.model.{FunctionMap, Uri}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

case class FunctionMapExtractor() extends ResourceExtractor[List[FunctionMap]] {

  lazy val triplesMapExtractor: TriplesMapExtractor = TriplesMapExtractor()

  /**
    * Extract.
    *
    * @param node Node to extract from.
    * @return
    */
  override def extract(node: RDFResource): List[FunctionMap] = {
    val properties = node.listProperties(RMLVoc.Property.OBJECTMAP)
    properties.flatMap {
      case literal: RDFLiteral =>
        throw new RMLException(literal.toString +
          ": A literal cannot be converted to a predicate object map")

      case resource: RDFResource =>
        resource.getType match {
          case Some(Uri(RMLVoc.Class.FUNCTIONTERMMAP)) => Some(extractFunctionMap(resource))
          case _ => None
        }

    }
  }

  private def extractFunctionMap(resource: RDFResource): FunctionMap = {
    val functionValues = resource.listProperties(RMLVoc.Property.FUNCTIONVALUE)

    require(functionValues.size == 1, "Only 1 function value allowed.")
    require(functionValues.head.isInstanceOf[RDFResource], "FunctionValue must be a resource.")

    val functionValue = functionValues.head.asInstanceOf[RDFResource]
    val triplesMap = triplesMapExtractor.extractTriplesMapProperties(functionValue)
    require(triplesMap.isDefined)
    FunctionMap(functionValue.uri.toString, triplesMap.get)
  }

}
