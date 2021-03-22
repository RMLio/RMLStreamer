/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/
package io.rml.framework.flink.source


import io.rml.framework.core.model.TCPSocketStream
import io.rml.framework.core.util.StreamerConfig
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object StreamUtil {

  def createTcpSocketSource(tCPSocketStream: TCPSocketStream, delimiter:String="\n")(implicit env: StreamExecutionEnvironment): DataStream[String] = {
    new DataStream[String](env.getJavaEnv.socketTextStream(tCPSocketStream.hostName, tCPSocketStream.port, delimiter))
  }

  /**
    * Enables local parallelisation on the stream over multiple task slots by adding a keyBy operation,
    * if executeLocalParallel (--local-parallel) is enabled.
    * @param inputStream The input data stream
    * @return A stream with the same items as the input stream, but potentially turned into a KeyedDataStream
    *         if parallelisation is enabled.
    */
  def paralleliseOverSlots(inputStream: DataStream[String]): DataStream[String] = {
    if (StreamerConfig.isExecuteLocalParallel()) {
      inputStream.keyBy(input => System.nanoTime())
    } else {
      inputStream
    }
  }

}
