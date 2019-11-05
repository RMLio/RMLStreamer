package io.rml.framework.core.model

import scala.collection.immutable

/**
  * A formatted RML Mapping that reorganizes the content of a normal RML Mapping for easier processing.
  */
trait FormattedRMLMapping extends RMLMapping {
  def containsStreamTriplesMaps(): Boolean
  def containsDatasetTriplesMaps(): Boolean


  /**
    * Standard triple maps are triple maps that come from a static data set and do not contain joins (i.e. are child)
    *
    * @return
    */
  def standardStaticTriplesMaps: List[TriplesMap]

  /**
    * Stream triple maps are triple maps that come from a streamed data
    *
    * @return
    */
  def standardStreamTriplesMaps: List[StreamTriplesMap]

  /**
    * Joined triple maps are triple maps extracted from triple maps with parent triple maps. Per joined triple map
    * there is one join condition with it's accompanying predicate object maps. A triple map with multiple join conditions
    * will be split into multiple joined triple maps. This is used for easier creating and managing joined pipelines.
    *
    * @return
    */
  def joinedStaticTriplesMaps: List[JoinedTriplesMap]

  /**
    * Joined triple maps are triple maps extracted from triple maps with parent triple maps. Per joined triple map
    * there is one join condition with it's accompanying predicate object maps. A triple map with multiple join conditions
    * will be split into multiple joined triple maps. This is used for easier creating and managing joined pipelines.
    *
    * @return
    */
  def joinedSteamTriplesMaps: List[JoinedTriplesMap]

}

case class StdFormattedRMLMapping(triplesMaps: List[TriplesMap],
                                  standardStaticTriplesMaps: List[TriplesMap],
                                  standardStreamTriplesMaps: List[StreamTriplesMap],
                                  identifier: String,
                                  containsParentTriplesMaps: Boolean,
                                  joinedStaticTriplesMaps: List[JoinedTriplesMap],
                                  joinedSteamTriplesMaps: List[JoinedTriplesMap]) extends FormattedRMLMapping() {

  def containsStreamTriplesMaps(): Boolean = standardStreamTriplesMaps.nonEmpty || joinedSteamTriplesMaps.nonEmpty

  def containsDatasetTriplesMaps(): Boolean = standardStaticTriplesMaps.nonEmpty || joinedStaticTriplesMaps.nonEmpty

}

object FormattedRMLMapping {

  // create a formatted mapping from a standard mapping
  def fromRMLMapping(mapping: RMLMapping): FormattedRMLMapping = {
    val triplesMaps = mapping.triplesMaps

    val staticTriplesMaps = triplesMaps.filter(!_.logicalSource.source.isInstanceOf[StreamDataSource])
    val (standardStaticTriplesMaps, joinedStaticTriplesMaps) = extractStandardAndJoinedTriplesMaps(staticTriplesMaps)

    val streamTriplesMaps = triplesMaps.filter(_.logicalSource.source.isInstanceOf[StreamDataSource])
    val (standardStreamTriplesMaps, joinedSteamTriplesMaps) = extractStandardAndJoinedTriplesMaps(streamTriplesMaps)


    StdFormattedRMLMapping(
      triplesMaps,
      standardStaticTriplesMaps,
      standardStreamTriplesMaps.asInstanceOf[List[StreamTriplesMap]],
      mapping.identifier,
      mapping.containsParentTriplesMaps,
      joinedStaticTriplesMaps,
      joinedSteamTriplesMaps)
  }

  private def extractStandardAndJoinedTriplesMaps(triplesMaps: List[TriplesMap]) = {
    // extract standard triple maps
    val standardTriplesMaps = triplesMaps.filter(!_.containsParentTriplesMap)

    // extract triple maps with parent triple maps
    val tmWithParentTM = triplesMaps.filter(_.containsParentTriplesMap)

    // extract all joined triple maps
    val joinedTriplesMaps = tmWithParentTM.flatMap(extractJoinedTriplesMapsFromTriplesMap)

    // extract all standard triple maps from a triple map that has parent triple maps
    val extractedStandardTriplesMaps = tmWithParentTM.map(extractStandardTriplesMapsFromTriplesMap)

    (standardTriplesMaps ++ extractedStandardTriplesMaps, joinedTriplesMaps)
  }

  /**
    * Extract one or more joined triple maps from triple maps
    *
    * @param triplesMap
    * @return
    */
  private def extractJoinedTriplesMapsFromTriplesMap(triplesMap: TriplesMap): List[JoinedTriplesMap] = {
    // get for every predicate object map, every object map, the parent triple map
    val list = triplesMap.predicateObjectMaps.flatMap(pm => pm.objectMaps.map(om => (pm, om, om.parentTriplesMap)))
    val newPoms: immutable.Iterable[PredicateObjectMap] = list.groupBy(item => item._3) // group by parent triple map
      .filter(item => item._1.isDefined) // filter out undefined parent triple maps

      // create new poms from these grouped poms by parent triple maps
      .flatMap(item => {
      item._2.map(item => PredicateObjectMap(item._1.identifier, List(item._2), item._1.functionMaps, item._1.predicateMaps, item._1.graphMap))
    })
    // every new pom will have exactly one parent triple map, create a JoinedTriplesMap from these poms
    newPoms.map(pom => {
      JoinedTriplesMap(TriplesMap(List(pom), triplesMap.logicalSource, triplesMap.subjectMap, triplesMap.identifier))
    }).toList

  }

  /**
    * Extract every standard triples map from a triples map, if there are parent triple maps, skip these.
    *
    * @param triplesMap  a triplesMap which might contain multiple predicateObjectMaps.
    * @return For every predicateObjectMap in the input triplesMap: a new triplesMap with one predicateObjectMap.
    *         PredicateObjectMaps with parentTriplesMap(s) are filtered out.
    */
  private def extractStandardTriplesMapsFromTriplesMap(triplesMap: TriplesMap): TriplesMap = {
    val list = triplesMap.predicateObjectMaps.flatMap(pm => pm.objectMaps.map(om => (pm, om, om.parentTriplesMap)))
    val newPoms = list.groupBy(item => item._3)
      .filter(item => item._1.isEmpty)
      .flatMap(item => {
        item._2.map(item => {
          PredicateObjectMap(item._1.identifier, List(item._2), item._1.functionMaps, item._1.predicateMaps,item._1.graphMap)
        })
      })
    TriplesMap(newPoms.toList, triplesMap.logicalSource, triplesMap.subjectMap, triplesMap.identifier)
  }

}

