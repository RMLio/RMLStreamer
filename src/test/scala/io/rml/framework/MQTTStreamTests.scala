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
  * */
package io.rml.framework

import io.moquette.broker.Server
import io.rml.framework.util.server.TestData
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import java.util.Properties
import scala.concurrent.ExecutionContextExecutor

object MQTTStreamTestsConfiguration {
  val VIRTUAL_HOST = "/"
  val HOSTNAME_BROKER = "localhost"
  val PORT = 1883
  val PROTOCOL = "tcp"
  val TOPIC_NAME = "topic"
  val QOS = 1
  val RETAINED = false
  val COMPLETION_TIMEOUT = 1000
}

class MQTTStreamTests extends StreamTestSync {
  var producer: MqttAsyncClient = _
  var broker: Server = _

  override def setup(): Unit = {
    setupBroker()
    setupProducer()
  }

  private def setupBroker() = {
    val mqttProps = new Properties
    mqttProps.put("port", MQTTStreamTestsConfiguration.PORT.toString);
    mqttProps.put("host", MQTTStreamTestsConfiguration.HOSTNAME_BROKER);
    broker = new Server();
    broker.startServer(mqttProps);
  }

  private def setupProducer() = {
    val protocol = MQTTStreamTestsConfiguration.PROTOCOL
    val host = MQTTStreamTestsConfiguration.HOSTNAME_BROKER
    val port = MQTTStreamTestsConfiguration.PORT
    val serverUri = s"${protocol}://${host}:${port}"
    this.producer = new MqttAsyncClient(serverUri, MqttAsyncClient.generateClientId(), new MemoryPersistence())
    logInfo("created MQTT Producer")
    this.producer.connect().waitForCompletion()
    logInfo("MQTT Producer connected")
  }

  override protected def passingTests: Array[(String, String)] = Array(
    (testFolder, "noopt")
  )

  override protected def testFolder: String = "sandbox/stream/mqtt"

  override protected def beforeTestCase(): Unit = {
    logInfo("before test case")
  }

  override protected def afterTestCase(): Unit = {
    logInfo("after test case")
  }

  override protected def teardown(): Unit = {
    logInfo("Tearing down MQTT Stream tests")
    broker.stopServer();
  }

  override protected def writeData(input: List[TestData])(implicit executor: ExecutionContextExecutor): Unit = {
    logInfo("writeData")
    for (batch <- input) {
      val topic = MQTTStreamTestsConfiguration.TOPIC_NAME
      val qos = MQTTStreamTestsConfiguration.QOS
      val retained = MQTTStreamTestsConfiguration.RETAINED
      val completionTimeout = MQTTStreamTestsConfiguration.COMPLETION_TIMEOUT
      for (in <- batch.data) {
        val deliveryToken = this.producer.publish(topic, in.getBytes, qos, retained)
        deliveryToken.waitForCompletion(completionTimeout)
        logInfo("messaged delivered...")
      }
    }
  }
}
