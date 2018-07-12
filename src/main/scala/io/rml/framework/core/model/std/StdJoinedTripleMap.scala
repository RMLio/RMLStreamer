package io.rml.framework.core.model.std

import io.rml.framework.core.model.{JoinedTripleMap, TermNode, TripleMap}

case class StdJoinedTripleMap(tripleMap: TripleMap) extends JoinedTripleMap(tripleMap) {
  /**
    *
    * @return
    */
  override def predicateObjectMaps = tripleMap.predicateObjectMaps

  /**
    *
    * @return
    */
  override def logicalSource = tripleMap.logicalSource

  /**
    *
    * @return
    */
  override def subjectMap = tripleMap.subjectMap

  /**
    *
    * @return
    */
  override def containsParentTripleMap = tripleMap.containsParentTripleMap

  override def identifier(): String = tripleMap.identifier

  /**
    *
    * @return
    */
  override def graphMap = ???
}
