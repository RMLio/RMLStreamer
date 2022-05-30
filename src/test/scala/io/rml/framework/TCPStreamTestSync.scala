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
package io.rml.framework
import io.rml.framework.util.logging.Logger
import io.rml.framework.util.server.TestData

import scala.concurrent.ExecutionContextExecutor

/**
  * @author Gerald Haesendonck
  */
class TCPStreamTestSync extends StreamTestSync {
  var tcpServer: TestTCPServer = _
  var serverThread: Thread = _;

  override def testFolder: String = "stream/tcp"

  override def passingTests: Array[(String, String)] = Array(
    (testFolder, "noopt")
  )


  override def setup(): Unit = {
    super.setup()
    tcpServer = new TestTCPServer(9999)
    serverThread = new Thread(tcpServer, "TCPServer")
    serverThread.start()
  }

  override def beforeTestCase(): Unit = {}

  override def afterTestCase(): Unit = {}

  override def teardown(): Unit = {
    Logger.logInfo("Stopping TCP server")
    if (serverThread != null) {
      tcpServer.stop()
      serverThread.join()
    }
    Logger.logInfo("TCP server stopped.")
  }

  override def writeData(input: List[TestData])(implicit executor: ExecutionContextExecutor): Unit = {
    for (batch <- input) {
      for (el <- batch.data) {
        tcpServer.send(el)
      }
    }
  }
}
