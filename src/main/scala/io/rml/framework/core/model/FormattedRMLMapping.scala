package io.rml.framework.core.model

import scala.collection.immutable

trait FormattedRMLMapping extends RMLMapping {

  def standardTripleMaps : List[TripleMap]

  def joinedTripleMaps : List[JoinedTripleMap]

}

case class StdFormattedRMLMapping(triplesMaps: List[TripleMap],
                                  uri: Uri,
                                  containsParentTripleMaps: Boolean,
                                  standardTripleMaps: List[TripleMap],
                                  joinedTripleMaps : List[JoinedTripleMap]) extends FormattedRMLMapping()

object FormattedRMLMapping {
  def fromRMLMapping(mapping: RMLMapping) :  FormattedRMLMapping = {
    val triplesMaps = mapping.triplesMaps
    val standardTripleMaps = triplesMaps.filter(!_.containsParentTripleMap)
    val tmWithParentTM = triplesMaps.filter(_.containsParentTripleMap)

    val joinedTripleMaps = tmWithParentTM.map(extractJoinedTripleMapsFromTripleMap)
    val extractedStandardTripleMaps = tmWithParentTM.map(extractStandardTripleMapsFromTripleMap)

    StdFormattedRMLMapping(mapping.triplesMaps,
                           mapping.uri,
                           mapping.containsParentTripleMaps,
                           standardTripleMaps ++ extractedStandardTripleMaps,
                           joinedTripleMaps)
  }

  private def extractJoinedTripleMapsFromTripleMap(tripleMap: TripleMap) : JoinedTripleMap = {
    val list = tripleMap.predicateObjectMaps.flatMap(pm => pm.objectMaps.map(om => (pm, om, om.parentTriplesMap)))
    val newPoms: immutable.Iterable[PredicateObjectMap] = list.groupBy(item => item._3)
                                                              .filter(item => item._1.isDefined)
                                                              .flatMap(item => {
                                                                item._2.map(item => PredicateObjectMap(item._1.uri, List(item._2) ,item._1.predicateMaps))
                                                              })
    JoinedTripleMap(TripleMap(newPoms.toList, tripleMap.logicalSource, tripleMap.subjectMap, tripleMap.uri))
  }

  private def extractStandardTripleMapsFromTripleMap(tripleMap: TripleMap) : TripleMap = {
    val list = tripleMap.predicateObjectMaps.flatMap(pm => pm.objectMaps.map(om => (pm, om, om.parentTriplesMap)))
    val newPoms = list.groupBy(item => item._3)
                      .filter(item => item._1.isEmpty)
                      .flatMap(item => {
                        item._2.map(item => {
                          PredicateObjectMap(item._1.uri, List(item._2), item._1.predicateMaps)
                        })
                      })
    TripleMap(newPoms.toList, tripleMap.logicalSource, tripleMap.subjectMap, tripleMap.uri)
  }

}

