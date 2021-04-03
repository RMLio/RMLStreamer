package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.{JoinConfigMapCache, JoinConfigMapExtractor}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.JoinConfigMap
import io.rml.framework.core.model.rdf.{RDFLiteral, RDFNode, RDFResource}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.engine.composers.JoinType
import io.rml.framework.engine.windows.WindowType
import io.rml.framework.shared.RMLException

case class StdJoinConfigMapExtractor(windowType: Option[WindowType]) extends JoinConfigMapExtractor with Logging {

  /**
   * Extract.
   *
   * @param resource Node to extract from.
   * @return
   */
  override def extract(resource: RDFResource): Option[JoinConfigMap] = {

    val properties = resource.listProperties(RMLVoc.Property.JOIN_CONFIG)
    val configMapOption = properties.map {
      case literal: RDFLiteral =>
        throw new RMLException(s"${literal}: A literal cannot be converted to join config map ")
      case resource: RDFResource => extractJoinConfigMap(resource)
    }
      .headOption
      .flatten

    configMapOption
  }


  private def extractJoinConfigMap(resource: RDFResource): Option[JoinConfigMap] = {


    val join_type = extractPropertiesEqualOne(resource, RMLVoc.Property.JOIN_TYPE).flatMap(node => JoinType.fromUri(node.toString))

    if (join_type.isEmpty) {
      None
    } else {

      Some(JoinConfigMap(resource.uri.toString, join_type.get, this.windowType))
    }

  }


  @throws(classOf[RMLException])
  private def extractPropertiesEqualOne(resource: RDFResource, property: String): Option[RDFNode] = {
    val properties = resource.listProperties(property)

    if (properties.size > 1)
      throw new RMLException(s"Given property ${property} cannot be defined more than once for JoinConfigMap.")

    properties.headOption
  }
}
