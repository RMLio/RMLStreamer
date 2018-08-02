package io.rml.framework.flink.source


import io.rml.framework.core.model.TCPSocketStream
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.netty.example.TcpReceiverSource

object StreamUtil {

  def createTcpSocketSource(tCPSocketStream: TCPSocketStream, delimiter:String="\n")(implicit env: StreamExecutionEnvironment): DataStream[String] = {

    tCPSocketStream._type match {
        //TODO Update flink to 1.3.3 to use latest methods in scala without java -> scala conversion.

      /**
        * Flink 1.3.2's scala socketTextStream can't use custom multi-char/String delimiter  (there is delimiter param but it's not being used)
        *
        */


      case TCPSocketStream.TYPE.PULL =>     new DataStream[String](env.getJavaEnv.socketTextStream(tCPSocketStream.hostName, tCPSocketStream.port, delimiter))
      case TCPSocketStream.TYPE.PUSH => env.addSource(new TcpReceiverSource(tCPSocketStream.port)).setParallelism(1) // to avoid library to setup multiple instances
    }
  }

}
