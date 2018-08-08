package io.rml.framework.util.server
import scala.concurrent.ExecutionContextExecutor

case class KafkaTestServer() extends TestServer {
  override def writeData(input: Iterable[String])(implicit executur: ExecutionContextExecutor): Unit = ???

  override def setup(): Unit = ???

  override def tearDown(): Unit = ???
}
