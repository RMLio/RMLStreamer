package io.rml.framework.core.model

import java.util.Objects

case class TCPSocketStream(hostName: String, port: Int, _type: String) extends StreamDataSource {
  override def uri: ExplicitNode = {
    val hashValue = Objects.hash(hostName, new Integer(port), _type)

    Uri(hashValue.toHexString)
  }
}

object TCPSocketStream {

  object TYPE {
    val PUSH = "PUSH"
    val PULL = "PULL"
  }

}
