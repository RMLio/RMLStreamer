package io.rml.framework.core.function.model

import java.lang.reflect.Method

import io.rml.framework.core.model.{Entity, Node, Uri}
import io.rml.framework.core.internal.Logging
trait Transformation extends Node with Logging{


  def name: Uri = Uri(identifier)

  def getMethod: Option[Method]

  //TODO: Doesn't support output of objects yet !
  // it currently only support string representable outputs!
  def execute(arguments: Map[Uri, String]): Option[Iterable[Entity]]

  def initialize(): Transformation  = {
    logInfo("initializing transformation")
    this
  }

}

object Transformation extends Logging{

  def apply(identifier:String, transformationMetaData: TransformationMetaData): Transformation={
    logInfo("Companion: Transformation - apply(identifier, transformationMetaData)")
    DynamicMethodTransformation(identifier, transformationMetaData)
  }
}
