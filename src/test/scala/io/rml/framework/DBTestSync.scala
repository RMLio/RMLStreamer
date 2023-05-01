package io.rml.framework
import io.rml.framework.containers.PostgresContainer
import io.rml.framework.util.logging.Logger
import io.rml.framework.util.server.TestData

import scala.concurrent.ExecutionContextExecutor

class DBTestSync extends StreamTestSync {
  var container: PostgresContainer = _
  override protected def testFolder: String = "db-tests"

  override protected def passingTests: Array[(String, String)] = Array((testFolder, "noopt"))

  override def setup(): Unit = {
    super.setup()
    container = new PostgresContainer()
    container.start()
  }

  override protected def beforeTestCase(testCaseName: String): Unit = {
    container.executeScript(s"${testCaseName}/resource.sql")
  }

  override protected def afterTestCase(testCaseName: String): Unit = {}

  override protected def teardown(): Unit = {
    container.stop()
  };

  override protected def writeData(input: List[TestData])(implicit executor: ExecutionContextExecutor): Unit = {}
}
