package io.rml.framework.util.server

import java.io.File
import java.util.Properties

import io.rml.framework.util.Logger
import kafka.admin.AdminUtils
import kafka.server.{KafkaConfig, KafkaServerStartable}
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.serialize.ZkSerializer
import org.I0Itec.zkclient.{ZkClient, ZkConnection}
import org.apache.commons.io.FileUtils
import org.apache.curator.test.TestingServer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.concurrent.ExecutionContextExecutor

case class KafkaTestServer() extends TestServer {

  var zk: Option[TestingServer] = None
  var kafka: Option[KafkaServerStartable] = None
  var zkClient: Option[ZkClient] = None
  var zkUtils: Option[ZkUtils] =  None
  val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](producerProps())
  val defaultTopic = "demo"
  var logDirs:Seq[String] = List()

  override def setup(): Unit = {
    val properties = serverProperties()
    zk = Some(new TestingServer(getZkPort(properties.getProperty("zookeeper.connect"))))
    zk.get.start()
    val config = new KafkaConfig(properties)

    logDirs =  config.logDirs
    kafka = Some(new KafkaServerStartable(config))


    kafka.get.startup()
    topicSetup(topicProps())

  }

  override def writeData(input: Iterable[String])(implicit executur: ExecutionContextExecutor): Unit = {
    for(in <- input){
      in.split("\n").foreach(Logger.logInfo)

      producer.send(new ProducerRecord[String,String](defaultTopic, in))
    }
  }

  override def tearDown(): Unit = {
    val props = topicProps()

    producer.close()
    if (zkClient.isDefined) {
      AdminUtils.deleteTopic(zkUtils.get, props.getProperty("topic"))
    }
    if (kafka.isDefined) kafka.get.shutdown()
    kafka.get.awaitShutdown()
    if (zk.isDefined) zk.get.close()
    cleanUpLogs()
  }

  //https://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api
  def topicSetup(prop: Properties): Unit = {
    val sessionTimeoutMs = 10000
    val connectionTimeoutMs = 10000
    val clientConnectionTuple = ZkUtils.createZkClientAndConnection(prop.getProperty("zookeeper.connect"), sessionTimeoutMs, connectionTimeoutMs)


    zkClient = Some(clientConnectionTuple._1)
    zkUtils = Some(new ZkUtils(clientConnectionTuple._1, clientConnectionTuple._2, false ))

    val topicName = prop.getProperty("topic")
    val numPartitions = 1
    val replicationFactor = 1
    val topicConfig = new Properties

    AdminUtils.createTopic(zkUtils.get, topicName, numPartitions, replicationFactor, topicConfig)
  }


  def cleanUpLogs(): Unit = {
    logDirs.foreach(dir => FileUtils.deleteDirectory(new File(dir)))

  }


  def topicProps(): Properties = {
    val props = serverProperties()
    props.put("topic", defaultTopic)
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

  private def getZkPort(inetAddress: String): Int = {
    if (inetAddress == null || inetAddress.isEmpty || !inetAddress.contains(":")) {
      throw new IllegalArgumentException("The given connection address in string is invalid.")
    }

    inetAddress.split(":")(1).toInt

  }

}
