package io.rml.framework.core.function.model

import java.lang.reflect.Method

import io.rml.framework.core.model.{Entity, Node, Uri}

trait Transformation extends Node {


  def name: Uri = Uri(identifier)

  def getMethod: Option[Method]

  //TODO: Doesn't support output of objects yet !
  // it currently only support string representable outputs!
  def execute(arguments: Map[Uri, String]): Option[Iterable[Entity]]

  def initialize(): Transformation  = this

}

object Transformation{

  def apply(identifier:String, transientTransformation: TransientTransformation): Transformation={
    DynamicMethodTransformation(identifier, transientTransformation)

  }
}
