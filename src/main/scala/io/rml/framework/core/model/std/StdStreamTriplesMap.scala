package io.rml.framework.core.model.std

import io.rml.framework.core.model.{StreamTriplesMap, TriplesMap}

case class StdStreamTriplesMap(triplesMap: TriplesMap) extends StreamTriplesMap(triplesMap)
