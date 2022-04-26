/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/

package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdObjectMap
import io.rml.framework.engine.windows.WindowType

/**
  * This trait represents an object-map.
  * An object-map is a term-map.
  *
  * Spec: http://rml.io/spec.html#predicate-map
  *
  */
trait ObjectMap extends TermMap {

  def joinCondition: Option[JoinCondition]
  def joinConfigMap: Option[String]
  def parentTriplesMap: Option[String]

}

case class JoinCondition(child: List[Literal], parent: List[Literal]) // TODO: make this a trait?

object ObjectMap {
  def apply(identifier: String,
            functionMap: List[FunctionMap] = List(),
            constant: Option[Entity] = None,
            reference: Option[Literal] = None,
            template: Option[Literal] = None,
            termType: Option[Uri] = None,
            datatype: Option[Uri] = None,
            language: Option[Literal] = None,
            windowType: Option[WindowType] = None,
            joinConfigMap: Option[String] = None,
            parentTriplesMap: Option[String] = None,
            joinCondition: Option[JoinCondition] = None,
            logicalTargets: Set[LogicalTarget]): ObjectMap =

    StdObjectMap(identifier,
      functionMap,
      constant,
      reference,
      template,
      termType,
      datatype,
      language,
      parentTriplesMap,
      joinCondition,
      joinConfigMap, 
      logicalTargets)
}
