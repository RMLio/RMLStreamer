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

import io.rml.framework.core.model.std.StdJoinedTriplesMap
import io.rml.framework.shared.RMLException

abstract class JoinedTriplesMap(triplesMap: TriplesMap) extends TriplesMap {

  private val parentTriplesMaps: List[String] = triplesMap.predicateObjectMaps.flatMap(pom => {
    pom.objectMaps.flatMap(om => {
      om.parentTriplesMap
    })
  })

  if (parentTriplesMaps.isEmpty) throw new RMLException(triplesMap.identifier +
    ": Can only create JoinedTriplesMap from triple map with parent triple maps.")

  if (parentTriplesMaps.exists(_ != parentTriplesMaps.head)) throw new RMLException(triplesMap.identifier +
    ": Can only create JoinedTriplesMap from triple map with one general parent triple map.")

  private val joinConditions: List[JoinCondition] = triplesMap.predicateObjectMaps.flatMap(pom => {
    pom.objectMaps.flatMap(om => {
      om.joinCondition
    })
  })

  if (joinConditions.exists(_ != joinConditions.head)) throw new RMLException(triplesMap.identifier +
    ": Can only create JoinedTripleMap from triple map with one general join condition.")

  val parentTriplesMap: String = parentTriplesMaps.head
  val joinCondition: Option[JoinCondition] = joinConditions.headOption

}

object JoinedTriplesMap {

  def apply(triplesMap: TriplesMap): JoinedTriplesMap = StdJoinedTriplesMap(triplesMap)

}
