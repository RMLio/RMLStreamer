package io.rml.framework.core.model

import scala.collection.immutable

trait FormattedRMLMapping extends RMLMapping {

  def standardTripleMaps : List[TripleMap]

  def streamTripleMaps : List[StreamTripleMap]

  def joinedTripleMaps : List[JoinedTripleMap]

}

case class StdFormattedRMLMapping(triplesMaps: List[TripleMap],
                                  streamTripleMaps: List[StreamTripleMap],
                                  uri: Uri,
                                  containsParentTripleMaps: Boolean,
                                  standardTripleMaps: List[TripleMap],
                                  joinedTripleMaps : List[JoinedTripleMap]) extends FormattedRMLMapping() {

  def containsStreamTripleMaps() : Boolean = streamTripleMaps.nonEmpty

  def containsDatasetTripleMaps() : Boolean = standardTripleMaps.nonEmpty

}

object FormattedRMLMapping {
  def fromRMLMapping(mapping: RMLMapping) :  FormattedRMLMapping = {
    val triplesMaps = mapping.triplesMaps
    val standardTripleMaps = triplesMaps.filter(!_.containsParentTripleMap)
                                        .filter(!_.logicalSource.source.isInstanceOf[StreamDataSource])
    val tmWithParentTM = triplesMaps.filter(_.containsParentTripleMap)
    val ptms: Seq[Uri] = tmWithParentTM.flatMap(tm => tm.predicateObjectMaps.flatMap(pm => pm.objectMaps.flatMap(om => om.parentTriplesMap))).map(item => item.uri)
    val streamTripleMaps = triplesMaps.filter(_.logicalSource.source.isInstanceOf[StreamDataSource])
                                        .map(item => StreamTripleMap.fromTripleMap(item))

    val joinedTripleMaps = tmWithParentTM.flatMap(extractJoinedTripleMapsFromTripleMap)
    val extractedStandardTripleMaps = tmWithParentTM.map(extractStandardTripleMapsFromTripleMap)

    StdFormattedRMLMapping(mapping.triplesMaps,
                           streamTripleMaps,
                           mapping.uri,
                           mapping.containsParentTripleMaps,
                           extractedStandardTripleMaps ++ standardTripleMaps.filter(tm => !ptms.contains(tm.uri)),
                           joinedTripleMaps)
  }

  private def extractJoinedTripleMapsFromTripleMap(tripleMap: TripleMap) : List[JoinedTripleMap] = {
    val list = tripleMap.predicateObjectMaps.flatMap(pm => pm.objectMaps.map(om => (pm, om, om.parentTriplesMap)))
    val newPoms: immutable.Iterable[PredicateObjectMap] = list.groupBy(item => item._3)
                                                              .filter(item => item._1.isDefined)
                                                              .flatMap(item => {
                                                                item._2.map(item => PredicateObjectMap(item._1.uri, List(item._2) ,item._1.functionMaps,item._1.predicateMaps))
                                                              })
    newPoms.map(pom => {
      JoinedTripleMap(TripleMap(List(pom), tripleMap.logicalSource, tripleMap.subjectMap, tripleMap.uri))
    }).toList

  }

  private def extractStandardTripleMapsFromTripleMap(tripleMap: TripleMap) : TripleMap = {
    val list = tripleMap.predicateObjectMaps.flatMap(pm => pm.objectMaps.map(om => (pm, om, om.parentTriplesMap)))
    val newPoms = list.groupBy(item => item._3)
                      .filter(item => item._1.isEmpty)
                      .flatMap(item => {
                        item._2.map(item => {
                          PredicateObjectMap(item._1.uri, List(item._2), item._1.functionMaps,  item._1.predicateMaps)
                        })
                      })
    TripleMap(newPoms.toList, tripleMap.logicalSource, tripleMap.subjectMap, tripleMap.uri)
  }

}

