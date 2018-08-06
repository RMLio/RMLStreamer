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
      case 1 => if (mapProperties.nonEmpty) {
        generalExtractGraph(mapProperties.head, extractGraphMap)
      } else {
        generalExtractGraph(shortcutProperties.head, extractGraph)
      }
      case _ => throw new RMLException("Only one GraphMap allowed.")
    }

  }


  override def extractTermType(resource: RDFResource): Option[Uri] = {
    val result = super.extractTermType(resource)
    if (result.isDefined) result else Some(Uri(RMLVoc.Class.IRI))
  }

  def generalExtractGraph(node: RDFNode, extractFunc: RDFResource => Option[GraphMap]): Option[GraphMap] = {
    val resource = node match {
      case literal: Literal => throw new RMLException("GraphMap must be a resource.")
      case resource: RDFResource => resource
    }

    resource.uri match {
      case Uri(RMLVoc.Property.DEFAULTGRAPH) => None
      case _ => extractFunc(resource)
    }

  }

  def extractGraph(resource: RDFResource): Option[GraphMap] = {

    Some(GraphMap(resource.uri.toString, Some(resource.uri), None, None, extractTermType(resource)))

  }

  def extractGraphMap(resource: RDFResource): Option[GraphMap] = {
    val termType = extractTermType(resource)
    val template = extractTemplate(resource)
    val constant = extractConstant(resource)
    val reference = extractReference(resource)
    Some(GraphMap(constant.getOrElse(resource.uri).toString, constant, reference, template, termType))
  }

}
