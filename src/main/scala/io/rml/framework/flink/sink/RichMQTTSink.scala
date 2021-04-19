package io.rml.framework.flink.sink

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.http.client.utils.URIBuilder
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import java.net.URI
import java.nio.charset.StandardCharsets

class RichMQTTSink(val broker: String, val topic: String) extends RichSinkFunction[String] {

  private var client: MqttClient = _

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)

    val uriBuilder = new URIBuilder()
    uriBuilder.setHost(broker)
    uriBuilder.setScheme("tcp")

    client = new MqttClient(uriBuilder.build.toString, MqttClient.generateClientId(), new MemoryPersistence)
    val connectionOptions = new MqttConnectOptions
    connectionOptions.setAutomaticReconnect(true)
    connectionOptions.setCleanSession(false)

    client.connect(connectionOptions)
  }

  override def invoke(value: String, context: SinkFunction.Context[_]): Unit = {
    val payload = value.getBytes(StandardCharsets.UTF_8)
    client.publish(topic, payload, 2, false)
  }

  override def close(): Unit = {
    client.disconnect()
  }
}
