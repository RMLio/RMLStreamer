package io.rml.framework.core.function.model.std

import java.lang.reflect.Method

import io.rml.framework.core.function.model.Function
import io.rml.framework.core.model.{Entity, Literal, Uri}
import io.rml.framework.core.vocabulary.RMLVoc

import scala.util.Random

case class StdRandomFunction(identifier:String = RMLVoc.Property.GREL_RANDOM) extends Function{
  private val random = new Random()

  override def execute(arguments: Map[Uri, String]): Option[Iterable[Entity]] = {
      Some(List(Literal(random.nextString(10))))
  }

  override def getMethod: Option[Method] = {
    None
  }
}
