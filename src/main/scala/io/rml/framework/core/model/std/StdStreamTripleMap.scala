package io.rml.framework.core.model.std

import io.rml.framework.core.model.{StreamTripleMap, TripleMap}

case class StdStreamTripleMap(tripleMap: TripleMap) extends StreamTripleMap(tripleMap)
