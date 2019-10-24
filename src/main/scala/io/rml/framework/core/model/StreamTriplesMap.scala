package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdStreamTriplesMap

abstract class StreamTriplesMap(triplesMap: TriplesMap) extends TriplesMap {

  require(triplesMap.logicalSource.source.isInstanceOf[StreamDataSource], "Source must be a stream.")
  //require(!tripleMap.containsParentTripleMap, "No parent triple maps allowed.")

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

object StreamTriplesMap {
  def fromTripleMap(triplesMap: TriplesMap): StdStreamTriplesMap = {
    StdStreamTriplesMap(triplesMap)
  }
}
