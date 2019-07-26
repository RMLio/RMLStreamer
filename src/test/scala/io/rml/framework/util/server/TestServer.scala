package io.rml.framework.util.server

import scala.concurrent.ExecutionContextExecutor

trait TestServer {

  def setup() :Unit

  def writeData(input: List[TestData])(implicit executur: ExecutionContextExecutor) : Unit

  def tearDown():Unit

}
