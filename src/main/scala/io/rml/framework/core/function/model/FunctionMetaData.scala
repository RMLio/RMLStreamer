package io.rml.framework.core.function.model

import java.lang.reflect.Method

import io.rml.framework.core.model.{Entity, Uri}

/**
 * Contains metadata of a function that can be called by an RML Mapping.
 *
 * @param source      string path of the source
 * @param className   class name containing the [[Function]]
 * @param methodName  method name of the [[Function]]
 * @param inputParam  [[List]] of input parameters for the [[Function]]
 * @param outputParam [[List]] of expected output parameters from the [[Function]]
 */
case class FunctionMetaData(source: String, className: String, methodName: String, inputParam: List[Parameter], outputParam: List[Parameter]) extends {

  /**
   * Used as an id for each inheriting objects for debugging ( maybe equivalence checking? e.g. in TermMap and TripleMap).
   *
   * @return
   */
  def identifier: String = s"($source, $className, $methodName, $inputParam, $outputParam)"
}
