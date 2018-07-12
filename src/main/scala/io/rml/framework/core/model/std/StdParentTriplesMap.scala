package io.rml.framework.core.model.std

import io.rml.framework.core.model.{ParentTriplesMap, TermNode, TripleMap}

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
    * cd f
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
