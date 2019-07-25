package io.rml.framework.util.server

import io.rml.framework.util.TestData

import scala.concurrent.ExecutionContextExecutor

trait TestServer {

  def setup() :Unit

  def writeData(input: List[TestData])(implicit executur: ExecutionContextExecutor) : Unit

  def tearDown():Unit

}
