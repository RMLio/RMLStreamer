package io.rml.framework.core.function.model

import java.lang.reflect.Method

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.{Entity, Node, Uri}
import io.rml.framework.flink.sink.FlinkRDFQuad
trait Function extends Node with Logging{


  def name: Uri = Uri(identifier)

  def getMethod: Option[Method]

  //TODO: Doesn't support output of objects yet !
  // it currently only support string representable outputs!
  def execute(paramTriples: List[FlinkRDFQuad]): Option[Iterable[Entity]]


  def execute(argumentsMap: Map[Uri, String]): Option[Iterable[Entity]]

  def initialize(): Function  = {
    logDebug("initializing Function")
    this
  }

}

object Function extends Logging{

  def apply(identifier:String, functionMetaData: FunctionMetaData): Function={
    logDebug("Companion: Function - apply(identifier, functionMetaData)")
    DynamicFunction(identifier, functionMetaData)
  }
}
