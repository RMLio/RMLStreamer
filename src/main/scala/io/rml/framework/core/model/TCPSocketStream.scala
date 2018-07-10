package io.rml.framework.core.model

case class TCPSocketStream(uri: Uri, hostName: String, port: Int, _type: String) extends StreamDataSource

object TCPSocketStream {

  object TYPE {
    val PUSH = "PUSH"
    val PULL = "PULL"
  }

}
