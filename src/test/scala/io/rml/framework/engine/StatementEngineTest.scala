package io.rml.framework.engine

import java.io.File

import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.model.FormattedRMLMapping
import io.rml.framework.engine.statement.StatementEngine
import org.scalatest.{FunSuite, Matchers}

class StatementEngineTest extends FunSuite with Matchers {

  test("example1") {
    val formattedMapping = readMapping("example1/example.rml.ttl")
    val engine = StatementEngine.fromTripleMaps(formattedMapping.standardTripleMaps)
    println("break")
  }

  private def readMapping(fileName:String): FormattedRMLMapping = {
    val classLoader = getClass.getClassLoader
    val file = new File(classLoader.getResource(fileName).getFile)
    val mapping = MappingReader().read(file)
    FormattedRMLMapping.fromRMLMapping(mapping)
  }

}
