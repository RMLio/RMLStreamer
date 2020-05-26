package io.rml.framework.core.function.model

import java.lang.reflect.Method

import io.rml.framework.core.model.{Entity, Uri}

/**
 * A case class which is made to hold the string values of class and method names
 * of a [[Transformation]] so that it can be initialized lazily later when needed in the rml mapping file
 *
 * @param source      string path of the source
 * @param className   class name containing the [[Transformation]]
 * @param methodName  method name of the [[Transformation]]
 * @param inputParam  [[List]] of input parameters for the [[Transformation]]
 * @param outputParam [[List]] of expected output parameters from the [[Transformation]]
 */
case class TransientTransformation(source: String, className: String, methodName: String, inputParam: List[Parameter], outputParam: List[Parameter]) extends
  Transformation {

  override def initialize(): Transformation = {
    Transformation(identifier, this)
  }

  override def getMethod: Option[Method] = None

  override def execute(arguments: Map[Uri, String]): Option[Iterable[Entity]] = None

  /**
   * Used as an id for each inheriting objects for debugging ( maybe equivalence checking? e.g. in TermMap and TripleMap).
   *
   * @return
   */
  override def identifier: String = s"($source, $className, $methodName, $inputParam, $outputParam)"


}
