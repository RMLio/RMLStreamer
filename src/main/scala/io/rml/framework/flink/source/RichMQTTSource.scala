package io.rml.framework.flink.source

import io.rml.framework.core.internal.Logging
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{IMqttMessageListener, MqttClient, MqttConnectOptions, MqttMessage}

import java.net.URI
import java.util.Properties
import scala.collection.JavaConversions._

/**
  *
  * @param properties
  * @tparam T
  */
case class RichMQTTSource(hypermediaTarget: String,
                          contentType: String,
                          controlPacketValue: String,
                          dup: String,
                          qos: Option[String]) extends RichSourceFunction[String] with Logging {

  private val properties = new Properties;
  properties.put("hypermediaTarget", hypermediaTarget);
  properties.put("contentType", contentType);
  properties.put("controlPacketValue", controlPacketValue);
  if (qos.isDefined) {
    properties.put("qos", qos.get);
  }
  properties.put("dup", dup);

  private var client: MqttClient = _

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)

    val hmTargetUri = new URI(properties.getProperty("hypermediaTarget"))
    var serverUri = hmTargetUri.toString
      // remove path (solves: URI path must be empty "tcp://mosquittobroker:1883/topic")
      .replace(hmTargetUri.getPath, "")
      // MqttClient expects the scheme to correspond to the underlying protocol which is used
      // it is assumed that TCP / SSL is used
      if (hmTargetUri.getScheme == "mqtt") {
        serverUri = serverUri.replaceFirst("mqtt", "tcp")
      } else if (hmTargetUri.getScheme == "mqtts") {
        serverUri = serverUri.replaceFirst("mqtts", "ssl")
      }

    // strips the first forward-slash from the path (e.g. /topicname becomes topicname)
    val topic = hmTargetUri.getPath.substring(1)

    properties.setProperty(MQTTPropertyKeys.SERVER_URI, serverUri)
    properties.setProperty(MQTTPropertyKeys.TOPIC, topic)

    logInfo("RichMQTTSource properties:")
    for (x <- properties.entrySet()) {
      logInfo(s"${x.getKey}: ${x.getValue}")
    }

    client = new MqttClient(properties.getProperty(MQTTPropertyKeys.SERVER_URI),
      properties.getProperty("clientId", MqttClient.generateClientId()),
      new MemoryPersistence())

    val connectOptions = createMqttConnectOptions(properties)
    client.connect(connectOptions)
  }

  protected def createMqttConnectOptions(properties: Properties) = {
    val connectOptions = new MqttConnectOptions()

    if (properties.containsKey(MQTTPropertyKeys.USERNAME)) connectOptions.setUserName(properties.getProperty(MQTTPropertyKeys.USERNAME))
    if (properties.containsKey(MQTTPropertyKeys.PASSWORD)) connectOptions.setPassword(properties.getProperty(MQTTPropertyKeys.PASSWORD).toCharArray)

    connectOptions.setAutomaticReconnect(true)
    connectOptions.setCleanSession(false)

    connectOptions
  }

  override def run(sourceContext: SourceFunction.SourceContext[String]): Unit = {
    val lock = sourceContext.getCheckpointLock
    val topicName = properties.getProperty(MQTTPropertyKeys.TOPIC)
    client.subscribe(topicName, new IMqttMessageListener {
      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        lock.synchronized {

          logDebug(s"Collecting MQTT Message [Thread: ${Thread.currentThread().getId}]" +
            s"\nmessage.isDuplicate: ${message.isDuplicate}" +
            s"\nmessage.isRetained: ${message.isRetained}")

          val payloadString = message.toString
          sourceContext.collect(payloadString)
        }
      }
    })

    while (true)
      Thread.sleep(100)
  }

  override def cancel(): Unit = {
    teardownClient()
  }

  override def close(): Unit = {
    super.close()
    teardownClient()
  }

  protected def teardownClient() = {
    // TODO: PROPERLY TEARDOWN CLIENT
    client.disconnect()
  }
}
