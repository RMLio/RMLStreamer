package io.rml.framework.util.server

import java.io.File
import java.util.Properties

import io.rml.framework.util.{FutureUtil, Logger, TestData}
import kafka.admin.AdminUtils
import kafka.server.{KafkaConfig, KafkaServerStartable}
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkClient
import org.apache.commons.io.FileUtils
import org.apache.curator.test.TestingServer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
case class KafkaTestServer(var topics:List[String]) extends TestServer {

  var zk: Option[TestingServer] = None
  var kafka: Option[KafkaServerStartable] = None
  var zkClient: Option[ZkClient] = None
  var zkUtils: Option[ZkUtils] = None
  val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](producerProps())
  val defaultTopic = "demo"
  var logDirs: Seq[String] = List()

  override def setup(): Unit = {
    val properties = serverProperties()
    zk = Some(new TestingServer(getZkPort(properties.getProperty("zookeeper.connect"))))
    zk.get.start()
    val config = new KafkaConfig(properties)

    logDirs = config.logDirs
    kafka = Some(new KafkaServerStartable(config))


    kafka.get.startup()
    topicSetup(topicProps(defaultTopic))

  }

  private def removeExtensions(fileName:String):String= {
    fileName.replaceAll("\\..*", "")
  }

  override def writeData(input: List[TestData])(implicit executur: ExecutionContextExecutor): Unit = {

    val edited = input.map( t => {
      val topic = removeExtensions(t.filename)
      val prop = topicProps(topic)
      topicSetup(prop)
      TestData(topic, t.data)
    })


    val futures = edited.map( batch => {

      val topic = if (edited.size == 1 ) defaultTopic else batch.filename
      Future {
       writeOneBatch(batch.data, topic)
      }
    })

    Await.result(FutureUtil.waitAll(futures),5 seconds)

  }

  def writeOneBatch(input: Iterable[String], topic: String = defaultTopic): Unit = {
    for (in <- input) {

      in.split("\n").foreach(Logger.logInfo)

      producer.send(new ProducerRecord[String, String](topic, in))

    }
  }

  override def tearDown(): Unit = {
    val props = topicProps(defaultTopic)

    producer.close()
    if (zkClient.isDefined) {

      for (t <- topics) {
        AdminUtils.deleteTopic(zkUtils.get, t)
      }
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
    zkUtils = Some(new ZkUtils(clientConnectionTuple._1, clientConnectionTuple._2, false))

    val topicName = prop.getProperty("topic")
    val numPartitions = 1
    val replicationFactor = 1
    val topicConfig = new Properties

    AdminUtils.createTopic(zkUtils.get, topicName, numPartitions, replicationFactor, topicConfig)
  }


  def cleanUpLogs(): Unit = {
    logDirs.foreach(dir => FileUtils.deleteDirectory(new File(dir)))

  }


  def topicProps(topic:String): Properties = {
    val props = serverProperties()
    props.put("topic", topic)
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
