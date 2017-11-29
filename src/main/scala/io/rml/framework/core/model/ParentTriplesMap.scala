package io.rml.framework.core.model

import io.rml.framework.core.model.std.StdParentTriplesMap

trait ParentTriplesMap extends TripleMap

object ParentTriplesMap {
  def apply(tripleMap: TripleMap): ParentTriplesMap = {
    StdParentTriplesMap(tripleMap)
  }

} 
