package io.rml.framework.spec_tests

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.containers.ContainerManager
import io.rml.framework.core.extractors.NodeCache
import io.rml.framework.core.model.DatabaseSource
import io.rml.framework.core.util.Util
import io.rml.framework.util.TestUtil
import io.rml.framework.util.logging.Logger
import io.rml.framework.{FunctionMappingTest, Main}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.{FunSuite, Matchers}
import org.testcontainers.containers.JdbcDatabaseContainer

import java.io.File

class PostgreSQL_Test extends FunSuite with Matchers with FunctionMappingTest {
  val USERNAME = "postgres"
  val PASSWORD = ""
  val companion = new Companion()

  test("RMLTC0000") {
    runTest("RMLTC0000")
  }
  test("RMLTC0001a") {
    runTest("RMLTC0001a")
  }
  test("RMLTC0001b") {
    runTest("RMLTC0001b")
  }
  test("RMLTC0002a") {
    runTest("RMLTC0002a")
  }
  test("RMLTC0002b") {
    runTest("RMLTC0002b")
  }
  test("RMLTC0002c") {
    runTest("RMLTC0002c")
  }
  test("RMLTC0002d") {
    runTest("RMLTC0002d")
  }
  //  test("RMLTC0002f") {
  //    runTest("RMLTC0002f")
  //  }
  test("RMLTC0002j") {
    runTest("RMLTC0002j")
  }
  test("RMLTC0002k") {
    runTest("RMLTC0002k")
  }
  test("RMLTC0003a") {
    runTest("RMLTC0003a")
  }
  test("RMLTC0003b") {
    runTest("RMLTC0003b")
  }
  test("RMLTC0003c") {
    runTest("RMLTC0003c")
  }
  test("RMLTC0004a") {
    runTest("RMLTC0004a")
  }
  test("RMLTC0005a") {
    runTest("RMLTC0005a")
  }
  test("RMLTC0005b") {
    runTest("RMLTC0005b")
  }
  test("RMLTC0006a") {
    runTest("RMLTC0006a")
  }
  test("RMLTC0007a") {
    runTest("RMLTC0007a")
  }
  test("RMLTC0007b") {
    runTest("RMLTC0007b")
  }
  test("RMLTC0007c") {
    runTest("RMLTC0007c")
  }
  test("RMLTC0007d") {
    runTest("RMLTC0007d")
  }
  test("RMLTC0007e") {
    runTest("RMLTC0007e")
  }
  test("RMLTC0007f") {
    runTest("RMLTC0007f")
  }
  test("RMLTC0007g") {
    runTest("RMLTC0007g")
  }
  test("RMLTC0008a") {
    runTest("RMLTC0008a")
  }
  test("RMLTC0008b") {
    runTest("RMLTC0008b")
  }
  test("RMLTC0008c") {
    runTest("RMLTC0008c")
  }
  test("RMLTC0009a") {
    runTest("RMLTC0009a")
  }
  test("RMLTC0009b") {
    runTest("RMLTC0009b")
  }
  test("RMLTC0009c") {
    runTest("RMLTC0009c")
  }
  test("RMLTC0009d") {
    runTest("RMLTC0009d")
  }
  test("RMLTC0010a") {
    runTest("RMLTC0010a")
  }
  test("RMLTC0010b") {
    runTest("RMLTC0010b")
  }
  test("RMLTC0010c") {
    runTest("RMLTC0010c")
  }
  //  test("RMLTC0011a") {
  //    runTest("RMLTC0011a")
  //  }
  test("RMLTC0011b") {
    runTest("RMLTC0011b")
  }
  test("RMLTC0012a") {
    runTest("RMLTC0012a")
  }
  test("RMLTC0012b") {
    runTest("RMLTC0012b")
  }
  test("RMLTC0012e") {
    runTest("RMLTC0012e")
  }
  test("RMLTC0013a") {
    runTest("RMLTC0013a")
  }
  //  test("RMLTC0013b") {
  //    runTest("RMLTC0013b")
  //  }
  test("RMLTC0014d") {
    runTest("RMLTC0014d")
  }
  //  test("RMLTC0015a") {
  //    runTest("RMLTC0015a")
  //  }
  test("RMLTC0016a") {
    runTest("RMLTC0016a")
  }
  //  test("RMLTC0016b") {
  //    runTest("RMLTC0016b")
  //  }
  test("RMLTC0016c") {
    runTest("RMLTC0016c")
  }
  test("RMLTC0016d") {
    runTest("RMLTC0016d")
  }
  test("RMLTC0016e") {
    runTest("RMLTC0016e")
  }
  test("RMLTC0018a") {
    runTest("RMLTC0018a")
  }
  test("RMLTC0019a") {
    runTest("RMLTC0019a")
  }
  test("RMLTC0019b") {
    runTest("RMLTC0019b")
  }
  test("RMLTC0020a") {
    runTest("RMLTC0020a")
  }
  test("RMLTC0020b") {
    runTest("RMLTC0020b")
  }
  test("RMLTC1027") {
    runTest("RMLTC1027")
  }


  def runTest(testName: String): Unit = {
    companion.setupDB(s"rml-testcases/${testName}-PostgreSQL/resource.sql")
    doMapping(s"rml-testcases/${testName}-PostgreSQL/mapping.ttl", companion.container)
    //    companion.stopContainer()
  }

  def doMapping(mappingPath: String, container: JdbcDatabaseContainer[_]): Unit = {
    NodeCache.clear()
    RMLEnvironment.setGeneratorBaseIRI(Some("http://example.com/base/"))

    val env = ExecutionEnvironment.getExecutionEnvironment
    val senv = StreamExecutionEnvironment.getExecutionEnvironment
    val testDir = Util.getFile(new File(mappingPath).getParent)

    // read mapping file
    val mapping = Util.readMappingFile(mappingPath)

    // inject the correct URL for the container
    for (map <- mapping.triplesMaps) {
      map.logicalSource.source.asInstanceOf[DatabaseSource].setURL(container.getJdbcUrl)
    }
    // create DataStream from the mapping
    val result = Main.createDataStreamFromFormattedMapping(mapping)(env, senv, postProcessor).executeAndCollect().toList

    // verify expected output
    val (expectedOut, expectedFormat) = TestUtil.getExpectedOutputs(testDir)

    val outcome = TestUtil.compareResults(s"Postgres test: ${mappingPath}", result, expectedOut, postProcessor.outputFormat, expectedFormat)
    outcome match {
      case Left(l) =>
        Logger.logError(l)
        fail()
      case Right(r) =>
        Logger.logSuccess(r)
    }
  }
}

/**
 * Companion class holding information about the container with the database
 */
class Companion {
  val container = ContainerManager.POSTGRES_CONTAINER
  container.start()
  val url: String = container.getJdbcUrl

  def setupDB(resourcePath: String): Unit = {
    val url = container.getJdbcUrl
    ContainerManager.executeScript(resourcePath, url, container.getUsername, container.getPassword)
  }
}
