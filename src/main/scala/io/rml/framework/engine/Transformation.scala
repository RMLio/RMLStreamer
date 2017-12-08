package io.rml.framework.engine

import io.rml.framework.core.model.{Parameters, Uri, Value}

trait Transformation {

  def name : Uri

  def execute(parameters: Parameters) : Option[Value]

}


