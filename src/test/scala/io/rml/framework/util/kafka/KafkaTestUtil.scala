package io.rml.framework.util.kafka

import java.io.File
import java.util.Properties

import kafka.admin.AdminUtils
import kafka.consumer.ConsumerConnector
import kafka.server.{KafkaConfig, KafkaServerStartable}
import kafka.utils.ZKStringSerializer
import org.I0Itec.zkclient.ZkClient
import org.apache.commons.io.FileUtils
import org.apache.curator.test.TestingServer
import org.apache.kafka.clients.producer.KafkaProducer


//https://gist.github.com/benstopford/49555b2962f93f6d50e3
object KafkaTestUtil {

  var producer: KafkaProducer[String, String] = _
  var consumerConnector: ConsumerConnector = _

  def setup(): Unit = {
    KafkaTestFixture.start(serverProperties())
  }

  def tearDown(): Unit = {
    KafkaTestFixture.stop()
  }

  def consumerProps(): Properties = {
    val props = new Properties()
    props.put("zookeeper.connect", serverProperties().get("zookeeper.connect"))
    props.put("group.id", "2")
    props.put("auto.offset.reset", "smallest")
    props
  }

  def producerProps(): Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("acks", "1")
    props
  }

  def serverProperties(): Properties = {
    val props = new Properties()
    props.put("zookeeper.connect", "localhost:2181")
    props.put("broker.id", "1")
    props.put("port", "9092")
    props.put("host.name", "localhost")
    props.put("delete.topic.enable", "true")
    props
  }

  private object KafkaTestFixture {
    var zk: Option[TestingServer] = None
    var kafka: Option[KafkaServerStartable] = None
    var zkClient: Option[ZkClient] = None
    var props:Properties = _
    def start(properties: Properties): Unit = {
      zk = Some(new TestingServer(properties.getProperty("zookeeper.connect").split(":")(1).toInt))
      zk.get.start()
      val config = new KafkaConfig(properties)
      kafka = Some(new KafkaServerStartable(config))

      properties.put("topic", "demo")
      kafka.get.startup()
      topicSetup(properties)
      props = properties

    }

    //https://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api
    def topicSetup(prop: Properties): Unit = {
      val sessionTimeoutMs = 10000
      val connectionTimeoutMs = 10000
      zkClient = Some(new ZkClient(prop.getProperty("zookeeper.connect"), sessionTimeoutMs, connectionTimeoutMs,
        ZKStringSerializer))


      val topicName = prop.getProperty("topic")
      val numPartitions = 1
      val replicationFactor = 1
      val topicConfig = new Properties
      AdminUtils.createTopic(zkClient.get, topicName, numPartitions, replicationFactor, topicConfig)
    }

    def cleanUpLogs(): Unit = {
      val logDirs = new KafkaConfig(props).logDirs

      logDirs.foreach(dir => FileUtils.deleteDirectory(new File(dir)))

    }

    def stop(): Unit = {
      if (kafka.isDefined) kafka.get.shutdown()
      kafka.get.awaitShutdown()
      if (zkClient.isDefined) {
        AdminUtils.deleteTopic(zkClient.get, "demo")
      }
      if (zk.isDefined) zk.get.close()


      cleanUpLogs()
    }
  }

}