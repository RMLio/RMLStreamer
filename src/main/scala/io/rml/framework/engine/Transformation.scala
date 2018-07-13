package io.rml.framework.engine

import io.rml.framework.core.model.{Entity, Parameters, Uri}

trait Transformation {

  def name: Uri

  def execute(parameters: Parameters): Option[Entity]

}


