package io.rml.framework.core.model.std

import io.rml.framework.core.model.{ParentTriplesMap, TripleMap}

case class StdParentTriplesMap(triplesMap: TripleMap) extends ParentTriplesMap {
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
    *cd f
    * @return
    */
  override def containsParentTripleMap = triplesMap.containsParentTripleMap

  override def uri = triplesMap.uri

  /**
    *
    * @return
    */
  override def graphMap = ???
}
