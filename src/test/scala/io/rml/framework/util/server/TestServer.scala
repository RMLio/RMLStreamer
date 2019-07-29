package io.rml.framework.util.server

import scala.concurrent.ExecutionContextExecutor

trait TestServer {


  def setup() :Unit

  def writeData(input: List[TestData])(implicit executur: ExecutionContextExecutor) : Unit

  /**
    * Tear down the server and close the server appropriately
    */
  def tearDown():Unit

  /**
    *
    * Reset the server state so that it can be reused for the next test
    * without having to call the expensive [[setup()]] method
    * Eg: Remove kafka topics and reset kafka broker state
    */
  def reset():Unit

}
