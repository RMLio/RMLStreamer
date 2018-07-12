package io.rml.framework.engine

import io.rml.framework.core.model.{Parameters, Uri, Entity}

trait Transformation {

  def name: Uri

  def execute(parameters: Parameters): Option[Entity]

}


