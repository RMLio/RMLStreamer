package io.rml.framework.util.server

trait StreamTestServerFactory {

  def createServer(test: String):TestServer
}

object TCPTestServerFactory extends StreamTestServerFactory {
  override def createServer(test: String): TestServer = {
      TCPTestServer()
  }
}

object KafkaTestServerFactory extends StreamTestServerFactory{
  def createServer(topics: List[String], test: String): TestServer = {
    KafkaTestServer(topics, test)
  }

  override def createServer(test: String):TestServer = {
    createServer(List("demo"), test)
  }
}


