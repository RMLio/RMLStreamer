package io.rml.framework.core.model

import scala.collection.immutable

/**
  * A formatted RML Mapping that reorganizes the content of a normal RML Mapping for easier processing.
  */
trait FormattedRMLMapping extends RMLMapping {

  /**
    * Standard triple maps are triple maps that come from a static data set and do not contain joins
    *
    * @return
    */
  def standardTripleMaps: List[TripleMap]

  /**
    * Stream triple maps are triple maps that come from a streamed data
    *
    * @return
    */
  def streamTripleMaps: List[StreamTripleMap]

  /**
    * Joined triple maps are triple maps extracted from triple maps with parent triple maps. Per joined triple map
    * there is one join condition with it's accompanying predicate object maps. A triple map with multiple join conditions
    * will be split into multiple joined triple maps. This is used for easier creating and managing joined pipelines.
    *
    * @return
    */
  def joinedTripleMaps: List[JoinedTripleMap]

}

case class StdFormattedRMLMapping(triplesMaps: List[TripleMap],
                                  streamTripleMaps: List[StreamTripleMap],
                                  identifier: TermNode,
                                  containsParentTripleMaps: Boolean,
                                  standardTripleMaps: List[TripleMap],
                                  joinedTripleMaps: List[JoinedTripleMap]) extends FormattedRMLMapping() {

  def containsStreamTripleMaps(): Boolean = streamTripleMaps.nonEmpty

  def containsDatasetTripleMaps(): Boolean = standardTripleMaps.nonEmpty

}

object FormattedRMLMapping {

  // create a formatted mapping from a standard mapping
  def fromRMLMapping(mapping: RMLMapping): FormattedRMLMapping = {
    val triplesMaps = mapping.triplesMaps

    // extract standard triple maps
    val standardTripleMaps = triplesMaps.filter(!_.containsParentTripleMap)
      .filter(!_.logicalSource.source.isInstanceOf[StreamDataSource])

    // extract triple maps with parent triple maps
    val tmWithParentTM = triplesMaps.filter(_.containsParentTripleMap)

    // extract all parent triple maps
    val ptms: Seq[TermNode] = tmWithParentTM.flatMap(tm => tm.predicateObjectMaps.flatMap(pm => pm.objectMaps.flatMap(om => om.parentTriplesMap))).map(item => item.identifier)

    // extract all triple maps with streamed data source
    val streamTripleMaps = triplesMaps.filter(_.logicalSource.source.isInstanceOf[StreamDataSource])
      .map(item => StreamTripleMap.fromTripleMap(item))

    // extract all joined triple maps
    val joinedTripleMaps = tmWithParentTM.flatMap(extractJoinedTripleMapsFromTripleMap)

    // extract all standard triple maps from a triple map that has parent triple maps
    val extractedStandardTripleMaps = tmWithParentTM.map(extractStandardTripleMapsFromTripleMap)

    StdFormattedRMLMapping(mapping.triplesMaps,
      streamTripleMaps,
      mapping.identifier,
      mapping.containsParentTripleMaps,
      extractedStandardTripleMaps ++ standardTripleMaps.filter(tm => !ptms.contains(tm.identifier)),
      joinedTripleMaps)
  }

  /**
    * Extract one or more joined triple maps from triple maps
    *
    * @param tripleMap
    * @return
    */
  private def extractJoinedTripleMapsFromTripleMap(tripleMap: TripleMap): List[JoinedTripleMap] = {
    // get for every predicate object map, every object map, the parent triple map
    val list = tripleMap.predicateObjectMaps.flatMap(pm => pm.objectMaps.map(om => (pm, om, om.parentTriplesMap)))
    val newPoms: immutable.Iterable[PredicateObjectMap] = list.groupBy(item => item._3) // group by parent triple map
      .filter(item => item._1.isDefined) // filter out undefined parent triple maps

      // create new poms from these grouped poms by parent triple maps
      .flatMap(item => {
      item._2.map(item => PredicateObjectMap(item._1.identifier, List(item._2), item._1.functionMaps, item._1.predicateMaps))
    })
    // every new pom will have exactly one parent triple map, create a JoinedTripleMap from these poms
    newPoms.map(pom => {
      JoinedTripleMap(TripleMap(List(pom), tripleMap.logicalSource, tripleMap.subjectMap, tripleMap.identifier))
    }).toList

  }

  /**
    * Extract every standard triple map from a triple map, if there are parent triple maps, skip these.
    *
    * @param tripleMap
    * @return
    */
  private def extractStandardTripleMapsFromTripleMap(tripleMap: TripleMap): TripleMap = {
    val list = tripleMap.predicateObjectMaps.flatMap(pm => pm.objectMaps.map(om => (pm, om, om.parentTriplesMap)))
    val newPoms = list.groupBy(item => item._3)
      .filter(item => item._1.isEmpty)
      .flatMap(item => {
        item._2.map(item => {
          PredicateObjectMap(item._1.identifier, List(item._2), item._1.functionMaps, item._1.predicateMaps)
        })
      })
    TripleMap(newPoms.toList, tripleMap.logicalSource, tripleMap.subjectMap, tripleMap.identifier)
  }

}

