package io.rml.framework.core.function.model.std

import io.rml.framework.core.function.model.Function
import io.rml.framework.core.model.{Entity, Literal, Uri}
import io.rml.framework.core.vocabulary.{Namespaces, RMLVoc}
import io.rml.framework.flink.sink.FlinkRDFQuad

import java.lang.reflect.Method

case class StdUpperCaseFunction(identifier: String = RMLVoc.Property.GREL_UPPERCASE) extends Function {
  override def execute(arguments: Map[Uri, String]): Option[Iterable[Entity]] = {
    val parameter = arguments.get(Uri(Namespaces("grel", "valueParameter")))

    parameter.map(string => List(Literal(string)))
  }

  override def execute(paramTriples: List[FlinkRDFQuad]): Option[Iterable[Entity]] = ???

  override def getMethod: Option[Method] = {
    None
  }
}
