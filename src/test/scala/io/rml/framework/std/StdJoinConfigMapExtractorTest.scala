package io.rml.framework.std

import java.io.File

import io.rml.framework.StaticTestSpec
import io.rml.framework.core.extractors.JoinConfigMapCache
import io.rml.framework.core.model.{FormattedRMLMapping, JoinConfigMap}
import io.rml.framework.core.util.Util
import io.rml.framework.util.fileprocessing.MappingTestUtil


class StdJoinConfigMapExtractorTest extends  StaticTestSpec{

  val mappingFile: File = Util.getFile("join-config-extract/mapping.ttl")
  val mapping: FormattedRMLMapping = MappingTestUtil.processFile(mappingFile)

  "Reading a TriplesMap with a join config map" should
    "result in appropriate config map generated in the generated joined triples map" in {

    val joinedStreamTriplesMap = mapping.joinedStreamTriplesMaps
    assert(joinedStreamTriplesMap.size == 1, "Exactly one joined triple map must be detected.")
    assert(joinedStreamTriplesMap.head.joinConfigMap.isDefined, "Join config map should be defined.")
    assert(joinedStreamTriplesMap.head.joinCondition.isDefined, "Join conditions should be defined.")

    val configMapKey = joinedStreamTriplesMap.head.joinConfigMap.get
    val configMapObject = JoinConfigMapCache(configMapKey)
    println(configMapObject.joinType)
  }

  it should "have cached the generated join config map" in {
    assert(JoinConfigMapCache.size == 1, "Extracted join config map should be cached.")
  }

}
