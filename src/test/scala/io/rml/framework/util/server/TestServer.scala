package io.rml.framework.util.server

import scala.concurrent.ExecutionContextExecutor

trait TestServer {
  def writeData(input: Iterable[String])(implicit executur: ExecutionContextExecutor) : Unit

  def setup() :Unit

  def tearDown():Unit

}
