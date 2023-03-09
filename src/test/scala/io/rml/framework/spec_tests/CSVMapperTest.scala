package io.rml.framework.spec_tests

import io.rml.framework.core.extractors.NodeCache
import io.rml.framework.core.util.Util
import io.rml.framework.util.TestUtil
import io.rml.framework.util.logging.Logger
import io.rml.framework.{FunctionMappingTest, Main}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.{FunSuite, Matchers}

import java.io.FileNotFoundException


/**
 * Class testing the RML testcases on CSV files
 */
class CSVMapperTest extends FunSuite with Matchers with FunctionMappingTest {

  test("RMLTC0000-CSV") {
    doMapping("RMLTC0000-CSV")
  }

  test("RMLTC0001a-CSV") {
    doMapping("RMLTC0001a-CSV")
  }

  test("RMLTC0001b-CSV") {
    doMapping("RMLTC0001b-CSV")
  }

  test("RMLTC0002a-CSV") {
    doMapping("RMLTC0002a-CSV")
  }

  test("RMLTC0002b-CSV") {
    doMapping("RMLTC0002b-CSV")
  }

  test("RMLTC0002c-CSV") {
    doMappingExpectError("RMLTC0002c-CSV")
  }

  test("RMLTC0002e-CSV") {
    doMappingExpectError("RMLTC0002e-CSV")
  }

  test("RMLTC0003c-CSV") {
    doMapping("RMLTC0003c-CSV")
  }

  test("RMLTC0004a-CSV") {
    doMapping("RMLTC0004a-CSV")
  }

  test("RMLTC0004b-CSV") {
    doMappingExpectError("RMLTC0004b-CSV")
  }

  test("RMLTC0005a-CSV") {
    doMapping("RMLTC0005a-CSV")
  }

  test("RMLTC0006a-CSV") {
    doMapping("RMLTC0006a-CSV")
  }

  test("RMLTC0007a-CSV") {
    doMapping("RMLTC0007a-CSV")
  }

  test("RMLTC0007b-CSV") {
    doMapping("RMLTC0007b-CSV")
  }

  test("RMLTC0007c-CSV") {
    doMapping("RMLTC0007c-CSV")
  }

  test("RMLTC0007d-CSV") {
    doMapping("RMLTC0007d-CSV")
  }

  test("RMLTC0007e-CSV") {
    doMapping("RMLTC0007e-CSV")
  }

  test("RMLTC0007f-CSV") {
    doMapping("RMLTC0007f-CSV")
  }

  test("RMLTC0007g-CSV") {
    doMapping("RMLTC0007g-CSV")
  }

    test("RMLTC0007h-CSV") {
      doMappingExpectError("RMLTC0007h-CSV")
    }

  test("RMLTC0008a-CSV") {
    doMapping("RMLTC0008a-CSV")
  }

  test("RMLTC0008b-CSV") {
    doMapping("RMLTC0008b-CSV")
  }

  test("RMLTC0008c-CSV") {
    doMapping("RMLTC0008c-CSV")
  }

  test("RMLTC0009a-CSV") {
    doMapping("RMLTC0009a-CSV")
  }

  test("RMLTC0009b-CSV") {
    doMapping("RMLTC0009b-CSV")
  }

  test("RMLTC0010a-CSV") {
    doMapping("RMLTC0010a-CSV")
  }

  test("RMLTC0010b-CSV") {
    doMapping("RMLTC0010b-CSV")
  }

  test("RMLTC0010c-CSV") {
    doMapping("RMLTC0010c-CSV")
  }

  test("RMLTC0011b-CSV") {
    doMapping("RMLTC0011b-CSV")
  }

  test("RMLTC0012a-CSV") {
    doMapping("RMLTC0012a-CSV")
  }

  test("RMLTC0012b-CSV") {
    doMapping("RMLTC0012b-CSV")
  }

    test("RMLTC0012c-CSV") {
      doMappingExpectError("RMLTC0012c-CSV")
    }

    test("RMLTC0012d-CSV") {
      doMappingExpectError("RMLTC0012d-CSV")
    }

  test("RMLTC0015a-CSV") {
    doMapping("RMLTC0015a-CSV")
  }

  test("RMLTC0015b-CSV") {
    doMappingExpectError("RMLTC0015b-CSV")
  }

  test("RMLTC0019a-CSV") {
    doMapping("RMLTC0019a-CSV")
  }

  test("RMLTC0019b-CSV") {
    doMapping("RMLTC0019b-CSV")
  }

  test("RMLTC0020a-CSV") {
    doMapping("RMLTC0020a-CSV")
  }

  test("RMLTC0020b-CSV") {
    doMapping("RMLTC0020b-CSV")
  }

  def doMapping(testDir: String): Unit = {
    NodeCache.clear()

    val env = ExecutionEnvironment.getExecutionEnvironment
    val senv = StreamExecutionEnvironment.getExecutionEnvironment
    val testDirectory = Util.getFile(s"src/test/resources/rml-testcases/${testDir}")

    // read the mapping file
    val mapping = Util.readMappingFile(s"${testDirectory}/mapping.ttl")

    val result = Main.createDataSetFromFormattedMapping(mapping)(env, senv, postProcessor).collect().toList

    val (expectedOutput, expectedFormat) = TestUtil.getExpectedOutputs(testDirectory)

    val outcome = TestUtil.compareResults(s"CSV test ${testDir}", result, expectedOutput, postProcessor.outputFormat, expectedFormat)

    outcome match {
      case Left(l) =>
        Logger.logError(l)
      case Right(r) =>
        Logger.logSuccess(r)
    }
  }

  def doMappingExpectError(testDir: String): Unit = {
    NodeCache.clear()

    val env = ExecutionEnvironment.getExecutionEnvironment
    val senv = StreamExecutionEnvironment.getExecutionEnvironment
    var testDirectory = Util.getFile(s"src/test/resources/rml-testcases-negative/${testDir}")
    if (!testDirectory.exists()) { // try to find the test in invalid mapping files
      testDirectory = Util.getFile(s"src/test/resources/rml-testcases-invalid-mapping-file/${testDir}")
    }

    if (!testDirectory.exists()) { // if it still doesn't exist, throw
      throw new FileNotFoundException(s"Can't find the test directory ${testDir}")
    }

    try {
      val mapping = Util.readMappingFile(s"${testDirectory}/mapping.ttl")

      val result = Main.createDataSetFromFormattedMapping(mapping)(env, senv, postProcessor).collect().toList
      Logger.logInfo(s"Generated output: ${result}")
    } catch {
      case e: Exception =>
        Logger.logSuccess(s"Exception ${e} caught!")
        return
    }

    Logger.logError("No exception has been thrown")
    fail()
  }
}
