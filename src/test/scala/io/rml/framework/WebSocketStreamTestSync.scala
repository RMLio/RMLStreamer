package io.rml.framework
import io.rml.framework.util.server.TestData

import scala.concurrent.ExecutionContextExecutor

/**
  * MIT License
  *
  * Copyright (C) 2017 - 2021 RDF Mapping Language (RML)
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
  * */
class WebSocketStreamTestSync extends StreamTestSync {
  var server: TestWebSocketServer = _


  override protected def testFolder: String = "sandbox/stream/websocket"

  override protected def passingTests: Array[(String, String)] = Array(
    (testFolder, "noopt")
  )


  override def setup(): Unit = {
    logInfo("Setting up TestWebSocketServer")
    super.setup()
    server = new TestWebSocketServer(9000)
    server.start()
  }

  override protected def beforeTestCase(): Unit = {
    logInfo("before test case")
  }

  override protected def afterTestCase(): Unit = {
    logInfo("after test case")
  }

  override protected def teardown(): Unit = {
    logInfo("Tearing down WebSocket Stream tests")
    server.stop();
  }

  override protected def writeData(input: List[TestData])(implicit executor: ExecutionContextExecutor): Unit = {
    logInfo("writeData")
    for (batch <- input) {
      for (in <- batch.data) {
        server.broadcast(in)
      }
    }
  }
}
