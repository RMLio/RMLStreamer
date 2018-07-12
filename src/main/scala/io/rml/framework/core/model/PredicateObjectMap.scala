/*
 * Copyright (c) 2017 Ghent University - imec
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdPredicateObjectMap


/**
  * This trait defines a predicate- object map.
  * A predicate-object map is a function that creates
  * one or more predicate-object pairs for each row/record/element/object of a logical source.
  * It is used in conjunction with a subject map to generate RDF triples in a triples map.
  * A predicate-object map is represented by a resource that references the following other resources:
  * - One or more predicate maps.
  * - One or more object maps or referencing object maps.
  *
  * Spec: http://rml.io/spec.html#predicate-object-map
  *
  */
trait PredicateObjectMap extends Node {
  def objectMaps: List[ObjectMap]

  def functionMaps: List[FunctionMap]

  def predicateMaps: List[PredicateMap]

}

object PredicateObjectMap {
  def apply(identifier: String, objectMaps: List[ObjectMap], functionMaps: List[FunctionMap], predicateMaps: List[PredicateMap]): PredicateObjectMap = {
    StdPredicateObjectMap(identifier, objectMaps, functionMaps, predicateMaps)
  }
}
