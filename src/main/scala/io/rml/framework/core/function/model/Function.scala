package io.rml.framework.core.function.model

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.rdf.SerializableRDFQuad
import io.rml.framework.core.model.{Entity, Node, Uri}
abstract class Function extends Node with Logging{


  def name: Uri = Uri(identifier)

  def execute(paramTriples: List[SerializableRDFQuad]): Option[Iterable[Entity]]

  @deprecated("Please use execute(paramTriples: List[FlinkRDFQuad]) instead")
  def execute(argumentsMap: Map[Uri, String]): Option[Iterable[Entity]]


}

object Function extends Logging{

  def apply(identifier:String, functionMetaData: FunctionMetaData): Function={
    logDebug("Companion: Function - apply(identifier, functionMetaData)")
    DynamicFunction(identifier, functionMetaData)
  }
}
