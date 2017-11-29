package io.rml.framework.flink

import io.rml.framework.core.model.rdf.{RDFLiteral, RDFResource}

class FlinkResource extends RDFResource {
  /**
    *
    * @param property
    * @return
    */
  override def listProperties(property: String) = ???

  /**
    *
    * @param property
    * @param resource
    */
  override def addProperty(property: String, resource: String) = ???

  /**
    *
    * @param property
    * @param resource
    */
  override def addProperty(property: String, resource: RDFResource) = ???

  /**
    *
    * @param property
    * @param literal
    */
  override def addLiteral(property: String, literal: String) = ???

  /**
    *
    * @param property
    * @param literal
    */
  override def addLiteral(property: String, literal: RDFLiteral) = ???

  override def uri = ???
}
