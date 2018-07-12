package io.rml.framework.core.extractors

import io.rml.framework.core.model.rdf.{RDFNode, RDFResource}
import io.rml.framework.core.model.{GraphMap, Literal}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

class StdGraphMapExtractor extends GraphMapExtractor {

  /**
    * Extract.
    *
    * @param node Node to extract from.
    * @return
    *
    */
  override def extract(node: RDFResource): Option[GraphMap] = {
    //TODO: rr:graph?

    val properties = node.listProperties(RMLVoc.Property.GRAPHMAP)

    properties.size match {
      case 0 => None
      case 1 => extractGraphMap(properties.head)
      case _ => throw new RMLException("Only one GraphMap allowed.")
    }

  }

  def extractGraphMap(node: RDFNode): Option[GraphMap] = {
    node match {
      case literal: Literal => throw new RMLException("GraphMap must be a resource.")
      case resource: RDFResource => Some(extractGraphMapProperties(resource))
    }
  }

  def extractGraphMapProperties(resource: RDFResource): GraphMap = {
    val termType = extractTermType(resource)
    val template = extractTemplate(resource)
    val constant = extractConstant(resource)
    val reference = extractReference(resource)
    GraphMap(resource.uri.toString, constant, reference, template, termType)
  }

}
