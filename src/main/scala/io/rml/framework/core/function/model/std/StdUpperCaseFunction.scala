package io.rml.framework.core.function.model.std

import io.rml.framework.core.function.model.Function
import io.rml.framework.core.model.{Entity, Literal, Uri}
import io.rml.framework.core.vocabulary.FunVoc
import io.rml.framework.flink.sink.FlinkRDFQuad

import java.lang.reflect.Method

case class StdUpperCaseFunction(identifier: String = FunVoc.GREL.Property.GREL_UPPERCASE) extends Function {
  override def execute(arguments: Map[Uri, String]): Option[Iterable[Entity]] = {
    val parameter = arguments.get(Uri(FunVoc.GREL.Property.GREL_VALUEPARAMETER));

    parameter.map(string => List(Literal(string)))
  }

  override def execute(paramTriples: List[FlinkRDFQuad]): Option[Iterable[Entity]] = ???

  override def getMethod: Option[Method] = {
    None
  }
}
