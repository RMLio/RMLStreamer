package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.JoinConditionExtractor
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.model.{JoinCondition, Literal}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.RMLException

class StdJoinConditionExtractor extends JoinConditionExtractor with Logging {

  /**
    * Extracts logical source from resource.
    *
    * @param node Resource to extract logical source from.
    * @return
    */
  @throws(classOf[RMLException])
  override def extract(node: RDFResource): Option[JoinCondition] = {

    logDebug(node.uri + ": Extracting join condition's parent & child.")

    for {
      parent <- extractParent(node)
      child <- extractChild(node)
    } yield JoinCondition(child, parent)

  }

  private def extractParent(resource: RDFResource): Option[Literal] = {

    val property = RMLVoc.Property.PARENT
    val properties = resource.listProperties(property)

    if (properties.size != 1)
      throw new RMLException(resource.uri + ": invalid amount of parent literals (amount=" + properties.size + ", should be 1 only).")

    properties.head match {
      case literal: Literal => Some(literal)
      case resource: RDFResource => throw new RMLException(resource.uri + ": parent must be a literal.")
    }

  }

  private def extractChild(resource: RDFResource): Option[Literal] = {
    val property = RMLVoc.Property.CHILD
    val properties = resource.listProperties(property)

    if (properties.size != 1)
      throw new RMLException(resource.uri + ": invalid amount of child literals (amount=" + properties.size + ", should be 1 only).")

    properties.head match {
      case literal: Literal => Some(literal)
      case resource: RDFResource => throw new RMLException(resource.uri + ": child must be a literal.")
    }
  }

}
