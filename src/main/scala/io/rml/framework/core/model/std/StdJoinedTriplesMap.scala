package io.rml.framework.core.model.std

import io.rml.framework.core.model.{JoinedTriplesMap, TriplesMap}

case class StdJoinedTriplesMap(triplesMap: TriplesMap) extends JoinedTriplesMap(triplesMap) {
  /**
    *
    * @return
    */
  override def predicateObjectMaps = triplesMap.predicateObjectMaps

  /**
    *
    * @return
    */
  override def logicalSource = triplesMap.logicalSource

  /**
    *
    * @return
    */
  override def subjectMap = triplesMap.subjectMap

  /**
    *
    * @return
    */
  override def containsParentTripleMap = triplesMap.containsParentTripleMap

  override def identifier(): String = triplesMap.identifier

  /**
    *
    * @return
    */
  override def graphMap = ???
}
