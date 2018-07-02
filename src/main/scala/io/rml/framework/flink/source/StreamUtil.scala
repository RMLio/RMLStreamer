package io.rml.framework.flink.source


import io.rml.framework.core.model.TCPSocketStream
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.netty.example.TcpReceiverSource

object StreamUtil {

  def createTcpSocketSource(tCPSocketStream: TCPSocketStream)(implicit env: StreamExecutionEnvironment) : DataStream[String] = {
    tCPSocketStream._type match {
      case TCPSocketStream.TYPE.PULL => env.socketTextStream(tCPSocketStream.hostName, tCPSocketStream.port)
      case TCPSocketStream.TYPE.PUSH => env.addSource(new TcpReceiverSource(tCPSocketStream.port)).setParallelism(1) // to avoid library to setup multiple instances
    }
  }

}
