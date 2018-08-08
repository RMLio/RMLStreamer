package io.rml.framework.util

import io.rml.framework.util.server.{KafkaTestServer, TestServer, TCPTestServer}

trait StreamTestServerFactory {

  def createServer():TestServer
}

object TCPTestServerFactory extends StreamTestServerFactory {
  override def createServer(): TestServer = {
      TCPTestServer()
  }
}

object KafkaTestServerFactory extends StreamTestServerFactory{
  override def createServer(): TestServer = {
    KafkaTestServer()
  }
}


