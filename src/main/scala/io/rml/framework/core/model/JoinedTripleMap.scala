package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdJoinedTripleMap
import io.rml.framework.shared.RMLException

abstract class JoinedTripleMap(tripleMap: TripleMap) extends TripleMap {

  private val parentTriplesMaps: List[TripleMap] = tripleMap.predicateObjectMaps.flatMap(pom => {
    pom.objectMaps.flatMap(om =>{
      om.parentTriplesMap
    })
  })

  if(parentTriplesMaps.isEmpty) throw new RMLException(tripleMap.uri +
    ": Can only create JoinedTripleMap from triple map with parent triple maps.")

  if(parentTriplesMaps.exists(_ != parentTriplesMaps.head)) throw new RMLException(tripleMap.uri +
    ": Can only create JoinedTripleMap from triple map with one general parent triple map.")

  private val joinConditions: List[JoinCondition] = tripleMap.predicateObjectMaps.flatMap(pom => {
    pom.objectMaps.flatMap(om =>{
      om.joinCondition
    })})

  if(joinConditions.exists(_ != joinConditions.head)) throw new RMLException(tripleMap.uri +
    ": Can only create JoinedTripleMap from triple map with one general join condition.")

  val parentTriplesMap: TripleMap = parentTriplesMaps.head
  val joinCondition: Option[JoinCondition] = joinConditions.headOption

}

object JoinedTripleMap {

  def apply(tripleMap: TripleMap): JoinedTripleMap = StdJoinedTripleMap(tripleMap)

}
