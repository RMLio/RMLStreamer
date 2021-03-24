package io.rml.framework.flink.sink

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class RichMQTTSink(val url: String, val topic: String) extends RichSinkFunction[String] {

  private var client: MqttClient = _

  override def open(parameters: Configuration): Unit = {
    super.open(parameters)

    client = new MqttClient(url, MqttClient.generateClientId(), new MemoryPersistence)
    val connectionOptions = new MqttConnectOptions
    connectionOptions.setAutomaticReconnect(true)
    connectionOptions.setCleanSession(false)

    client.connect(connectionOptions)
  }

  override def invoke(value: String, context: SinkFunction.Context[_]): Unit = {
    val payload = value.getBytes()
    client.publish(topic, payload, 2, false)
  }

  override def close(): Unit = {
    client.disconnect()
  }
}
