package io.rml.framework

import io.rml.framework.containers.PostgresContainer
import io.rml.framework.util.server.TestData

import scala.concurrent.ExecutionContextExecutor

class DBTestSync2 extends StreamTestSync2 {
  var container: PostgresContainer = _

  override def setup(): Unit = {
    super.setup()
    container = new PostgresContainer()
    container.start()
  }

  override protected def testFolder = "db-tests"

  override protected def passingTests: Array[(String, String)] = Array((s"$testFolder/positive", "noopt"))

  override protected def failingTests: Array[(String, String)] = Array((s"$testFolder/negative", "noopt"))

  override protected def beforeTestCase(testCaseName: String): Unit = {
    container.executeScript(s"${testCaseName}/resource.sql")
  }

  override protected def afterTestCase(testCaseName: String): Unit = {}

  override protected def teardown(): Unit = {
    container.stop()
  };

  override protected def writeData(input: List[TestData])(implicit executor: ExecutionContextExecutor): Unit = {}
}

