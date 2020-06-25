package io.rml.framework.core.function.model

import java.lang.reflect.Method

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.{Entity, Node, Uri}
import io.rml.framework.flink.sink.FlinkRDFQuad
abstract class Function extends Node with Logging{


  def name: Uri = Uri(identifier)

  def getMethod: Option[Method]


  def execute(paramTriples: List[FlinkRDFQuad]): Option[Iterable[Entity]]

  @deprecated("Please use execute(paramTriples: List[FlinkRDFQuad]) instead")
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
