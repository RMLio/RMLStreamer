package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdParentTriplesMap

trait ParentTriplesMap extends TriplesMap

object ParentTriplesMap {
  def apply(triplesMap: TriplesMap): ParentTriplesMap = {
    StdParentTriplesMap(triplesMap)
  }

} 
