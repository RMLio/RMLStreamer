package io.rml.framework.engine

import java.io.File

import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.FormattedRMLMapping
import org.scalatest.{FunSuite, Matchers}

class StatementEngineTest extends FunSuite with Matchers {

  test("example1") {
    val classLoader = getClass.getClassLoader
    val file = new File(classLoader.getResource("example1/example.rml.ttl").getFile)
    val mapping = MappingReader().read(file)
    val formattedMapping = FormattedRMLMapping.fromRMLMapping(mapping)
    println("break")
  }

}
