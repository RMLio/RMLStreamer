package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdJoinedTriplesMap
import io.rml.framework.shared.RMLException

abstract class JoinedTriplesMap(triplesMap: TriplesMap) extends TriplesMap {

  private val parentTriplesMaps: List[TriplesMap] = triplesMap.predicateObjectMaps.flatMap(pom => {
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

  val parentTriplesMap: TriplesMap = parentTriplesMaps.head
  val joinCondition: Option[JoinCondition] = joinConditions.headOption

}

object JoinedTriplesMap {

  def apply(triplesMap: TriplesMap): JoinedTriplesMap = StdJoinedTriplesMap(triplesMap)

}
