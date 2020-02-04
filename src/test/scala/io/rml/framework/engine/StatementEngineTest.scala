package io.rml.framework.engine

import java.io.File

import io.rml.framework.Main
import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.util.Util
import io.rml.framework.util.TestUtil
import io.rml.framework.util.logging.Logger
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.scalatest.{FunSuite, Matchers}

class StatementEngineTest extends FunSuite with Matchers {

  private def executeTest(mappingFile: String): Unit = {
    RMLEnvironment.setGeneratorBaseIRI(Some("http://example.org/base/"))
    implicit val env = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment
    implicit val postProcessor = new NopPostProcessor()

    // read the mapping
    val formattedMapping = Util.readMappingFile(mappingFile)

    // execute
    val result = Main.createDataSetFromFormattedMapping(formattedMapping).collect()

    // get expected output
    val testDir = new File(mappingFile).getParentFile.getAbsoluteFile
    val (expectedOutput, expectedOutputFormat) = TestUtil.getExpectedOutputs(testDir)

    val testOutcome = TestUtil.compareResults(s"StatementEngineTest: ${testDir}", result, expectedOutput, postProcessor.outputFormat, expectedOutputFormat)
    testOutcome match {
      case Left(e) => {
        Logger.logError(e)
        System.exit(1)
        fail(e)
      }
      case Right(e) => {
        Logger.logSuccess(e)
      }
    }
  }


  test("example10") {
    pending // TODO: fix issue 77 first
    executeTest("example10/mapping.rml.ttl")
  }

  test("example1") {
    executeTest("example1/example.rml.ttl")
  }

  test("example2") {
    executeTest("example2/example.rml.ttl")
  }

  test("example2-bn") {
    executeTest("example2-bn/example.rml.ttl")
  }

  test("example2-lang") {
    executeTest("example2-lang/example.rml.ttl")
  }

  test("example2-object") {
    executeTest("example2-object/example.rml.ttl")
  }

  test("example2-pm") {
    executeTest("example2pm/example.rml.ttl")
  }

  test("example3") {
    pending // TODO: no multiple joins supported yet
    executeTest("example3/example3.rml.ttl")
  }

  test("example4") {
    executeTest("example4/example4_Venue.rml.ttl")
  }

  test("example4b") {
    executeTest("example4b/example4_Venue.rml.ttl")
  }

  test("example6") {
    executeTest("example6/example.rml.ttl")
  }

  test("example8") {
    executeTest("example8/simergy.rml.ttl")
  }

  test("csv-special-character-headers") {
    pending //TODO: multiple join conditions are not supported yet
    executeTest("csv-extensive-1/complete.rml.ttl")
  }

}