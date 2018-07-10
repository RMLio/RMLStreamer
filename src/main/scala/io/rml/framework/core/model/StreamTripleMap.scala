package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdStreamTripleMap

abstract class StreamTripleMap(tripleMap: TripleMap) extends TripleMap {

  require(tripleMap.logicalSource.source.isInstanceOf[StreamDataSource], "Source must be a stream.")
  require(!tripleMap.containsParentTripleMap, "No parent triple maps allowed.")

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

  override def identifier(): TermNode = tripleMap.identifier

  /**
    *
    * @return
    */
  override def graphMap = ???

}

object StreamTripleMap {
  def fromTripleMap(tripleMap: TripleMap): StdStreamTripleMap = {
    StdStreamTripleMap(tripleMap)
  }
}
