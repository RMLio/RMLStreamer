package io.rml.framework.flink.source


import io.rml.framework.core.model.TCPSocketStream
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.netty.example.TcpReceiverSource

object StreamUtil {

  def createTcpSocketSource(tCPSocketStream: TCPSocketStream, delimiter:Char='\n')(implicit env: StreamExecutionEnvironment): DataStream[String] = {


    tCPSocketStream._type match {
        //TODO: choose delimiter when mapper is gonna process csv data
      case TCPSocketStream.TYPE.PULL => env.socketTextStream(tCPSocketStream.hostName, tCPSocketStream.port, delimiter)
      case TCPSocketStream.TYPE.PUSH => env.addSource(new TcpReceiverSource(tCPSocketStream.port)).setParallelism(1) // to avoid library to setup multiple instances
    }
  }

}
