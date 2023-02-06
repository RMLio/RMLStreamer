package io.rml.framework

import io.rml.framework.containers.ContainerManager
import io.rml.framework.core.extractors.NodeCache
import io.rml.framework.core.model.DatabaseSource
import io.rml.framework.core.util.Util
import io.rml.framework.util.TestUtil
import io.rml.framework.util.logging.Logger
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.{FunSuite, Matchers}
import org.testcontainers.containers.JdbcDatabaseContainer

import java.io.File

class PostgreSQL_Test extends FunSuite with Matchers with FunctionMappingTest {

  final val USERNAME = "postgres"
  final val PASSWORD = ""

  var container: JdbcDatabaseContainer[_] = null

  def startPostgres(testDir: String): Unit = {
    this.container = ContainerManager.getPostgresContainer("postgres:latest", testDir)
  }

  def runTest(path: String): Unit = {
    NodeCache.clear()

    val env = ExecutionEnvironment.getExecutionEnvironment
    val senv = StreamExecutionEnvironment.getExecutionEnvironment
    val testDir = Util.getFile(new File(path).getParent)

    // fire up the container
    startPostgres("rml-testcases/RMLTC0000-PostgreSQL/resource.sql")

    // read mapping file
    val mapping = Util.readMappingFile(path)

    mapping.triplesMaps.head.logicalSource.source.asInstanceOf[DatabaseSource].setURL(this.container.getJdbcUrl)
    // create DataStream from the mapping
    val result = Main.createDataStreamFromFormattedMapping(mapping)(env, senv, postProcessor).executeAndCollect().toList

    // verify expected output
    val (expectedOut, expectedFormat) = TestUtil.getExpectedOutputs(testDir)

    val outcome = TestUtil.compareResults(s"Postgres test: ${path}", result, expectedOut, postProcessor.outputFormat, expectedFormat)
    outcome match {
      case Left(l) =>
        Logger.logError(l)
        fail()
      case Right(r) =>
        Logger.logSuccess(r)
    }
  }

  test("myTest") {
    runTest("rml-testcases/RMLTC0000-PostgreSQL/mapping.ttl")
  }
}
