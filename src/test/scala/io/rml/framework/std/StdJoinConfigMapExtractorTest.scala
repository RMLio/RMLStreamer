package io.rml.framework.std

import java.io.File

import io.rml.framework.StaticTestSpec
import io.rml.framework.core.util.Util
import io.rml.framework.util.fileprocessing.MappingTestUtil


class StdJoinConfigMapExtractorTest extends  StaticTestSpec{

  val mappingFile = Util.getFile("join-config-extract/mapping.ttl")
  "Reading a TriplesMap with a join config map" should
    "result in config map generated in the generated joined triples map" in {

    val mapping = MappingTestUtil.processFile(mappingFile)

    val joinedStreamTriplesMap = mapping.joinedStreamTriplesMaps
    assert(joinedStreamTriplesMap.size == 1, "Exactly one joined triple map must be detected.")
    assert(joinedStreamTriplesMap.head.joinConfigMap.isDefined, "Join config map should be defined.")
    assert(joinedStreamTriplesMap.head.joinCondition.isDefined, "Join conditions should be defined.")

  }

}
