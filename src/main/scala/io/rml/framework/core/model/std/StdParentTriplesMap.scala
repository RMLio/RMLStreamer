package io.rml.framework.core.model.std

import io.rml.framework.core.model.{ParentTriplesMap, TriplesMap}

case class StdParentTriplesMap(triplesMap: TriplesMap) extends ParentTriplesMap {
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
    * cd f
    *
    * @return
    */
  override def containsParentTriplesMap = triplesMap.containsParentTriplesMap

  override def identifier(): String = triplesMap.identifier

  /**
    *
    * @return
    */
  override def graphMap = ???
}
