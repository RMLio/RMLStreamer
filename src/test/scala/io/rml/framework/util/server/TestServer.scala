package io.rml.framework.util.server

import scala.concurrent.ExecutionContextExecutor

trait TestServer {

  def setup() :Unit

  def writeData(input: Iterable[String])(implicit executur: ExecutionContextExecutor) : Unit

  def tearDown():Unit

}
