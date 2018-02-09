package io.rml.framework.integration

import java.io.File

import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.internal.Logging
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class ParentTriplesMapIntegrationTest extends FunSuite with Matchers
                                                       with MockitoSugar
                                                       with BeforeAndAfter
                                                       with Logging {


  /**
  test("testReadFromFile") {

    // Read mapping file
    val file = new File("/home/wmaroy/framework/src/main/resources/json_data_mapping.ttl")
    val mapping = MappingReader().read(file)

    val parentTripleMaps = mapping.triplesMaps.flatMap(tm =>
      tm.predicateObjectMaps.flatMap(pm =>
        pm.objectMaps.flatMap(om => om.parentTriplesMap)))

    parentTripleMaps.isEmpty should be (false)



    println(parentTripleMaps.head.toString + " is a parent triples map.") // break statement

  }

    **/
  /**
  test("testReadFromFile - test containsParentTripleMap method") {

    // Read mapping file
    val file = new File("/home/wmaroy/framework/src/main/resources/json_data_mapping.ttl")
    val mapping = MappingReader().read(file)

    mapping.containsParentTripleMaps should be (true)

  }

    **/

}
