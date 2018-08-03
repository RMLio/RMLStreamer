package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.GraphMapExtractor
import io.rml.framework.core.model.rdf.{RDFNode, RDFResource}
import io.rml.framework.core.model.{GraphMap, Literal, Uri}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

class StdGraphMapExtractor extends GraphMapExtractor {

  /**
    * Extract.
    *
    * @param resource Resource to extract from.
    * @return
    *
    */
  override def extract(resource: RDFResource): Option[GraphMap] = {
    val mapProperties = resource.listProperties(RMLVoc.Property.GRAPHMAP)
    val shortcutProperties = resource.listProperties(RMLVoc.Property.GRAPH)
    val amount = mapProperties.size + shortcutProperties.size


    amount match {
      case 0 => None
      case 1 => if (mapProperties.nonEmpty) extractGraphMap(mapProperties.head) else extractGraph(shortcutProperties.head)
      case _ => throw new RMLException("Only one GraphMap allowed.")
    }

  }

  private def getRDFResource(node: RDFNode): RDFResource = {
    node match {
      case literal: Literal => throw new RMLException("GraphMap must be a resrouce.")
      case resource: RDFResource => resource
    }
  }

  override def extractTermType(resource: RDFResource): Option[Uri] = {
    val result = super.extractTermType(resource)
    if (result.isDefined) result else Some(Uri(RMLVoc.Class.IRI))
  }

  def extractGraph(node: RDFNode): Option[GraphMap] = {
    val resource = getRDFResource(node)
    Some(GraphMap(resource.uri.toString, Some(Uri(resource.uri.toString)), None, None, extractTermType(resource)))

  }

  def extractGraphMap(node: RDFNode): Option[GraphMap] = {
    Some(extractGraphMapProperties(getRDFResource(node)))
  }

  def extractGraphMapProperties(resource: RDFResource): GraphMap = {
    val termType = extractTermType(resource)
    val template = extractTemplate(resource)
    val constant = extractConstant(resource)
    val reference = extractReference(resource)
    GraphMap(constant.getOrElse(resource.uri).toString, constant, reference, template, termType)
  }

}
