package io.rml.framework.util.server

import java.util.Properties

import io.rml.framework.util.TestProperties
import io.rml.framework.util.logging.Logger
import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.I0Itec.zkclient.ZkClient
import org.apache.commons.io.FileUtils
import org.apache.curator.test.TestingServer
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContextExecutor, Future}

case class KafkaTestServer(var topics: List[String], test: String) extends TestServer {

  var zk: Option[TestingServer] = None
  var kafka: Option[KafkaServerStartable] = None
  var zkClient: Option[ZkClient] = None
  var adminClient: Option[AdminClient] = None
  val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](producerProps())
  val defaultTopic = "demo"
  val defaultDir: String = TestProperties.getTempDir(test).getAbsolutePath

  override def setup(): Unit = {
    val properties = serverProperties()
    zk = Some(new TestingServer(getZkPort(properties.getProperty("zookeeper.connect"))))
    zk.get.start()
    val config = new KafkaConfig(properties)

    kafka = Some(new KafkaServerStartable(config))
    kafka.get.startup()
    adminClient = Some(AdminClient.create(properties))
    topicSetup(topicProps(defaultTopic))
  }

  private def removeExtensions(fileName: String): String = {
    fileName.replaceAll("\\..*", "")
  }

  override def writeData(input: List[TestData])(implicit executur: ExecutionContextExecutor): Unit = {

    /**
      * Create topics for each test data defined by TestData(topic, data)
      * and add them to [[topics]]
      */
    val edited = input.map(t => {
      val topic = removeExtensions(t.filename)
      val prop = topicProps(topic)
      topicSetup(prop)

      topics ::= topic
      TestData(topic, t.data)
    })


    edited.map(batch => {

      val topic = if (edited.size == 1) defaultTopic else batch.filename
      Future {
        writeOneBatch(batch.data, topic)
      }
    })

  }

  /**
    * Write one batch of data to a topic
    *
    * @param input list of input data
    * @param topic kafka topic to which the `input` will be written to
    */
  def writeOneBatch(input: Iterable[String], topic: String = defaultTopic): Unit = {
    for (in <- input) {

      in.split("\n").foreach(Logger.logInfo)

      producer.send(new ProducerRecord[String, String](topic, in))

    }
  }

  override def reset(): Unit = {
    if (zkClient.isDefined) {
      adminClient.get.deleteTopics(topics.asJava)
    }
    topics = List(defaultTopic)
  }

  override def tearDown(): Unit = {
    producer.close()
    if (adminClient.isDefined)  {
      Logger.logInfo(s"Server deleting topics: $topics")
      adminClient.get.deleteTopics(topics.asJava)
      adminClient.get.close()
      Logger.logInfo("Server finish deleting kafka topics! ")
    }
    if (kafka.isDefined) kafka.get.shutdown()
    if (zk.isDefined) zk.get.close()
    cleanUpLogs()
  }

  //https://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api
  def topicSetup(prop: Properties): Unit = {
    val topicName = prop.getProperty("topic")
    val numPartitions = 1
    val replicationFactor = 1
    val topic = new NewTopic(topicName, numPartitions, replicationFactor.toShort)
    adminClient.get.createTopics(ArrayBuffer(topic).asJava)
  }

  def cleanUpLogs(): Unit = {
    FileUtils.deleteDirectory(FileUtils.getFile(defaultDir))
  }

  def topicProps(topic: String): Properties = {
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
    props.put("bootstrap.servers", "localhost:9092")
    props.put("broker.id", "1")
    props.put("port", "9092")
    props.put("log.dir", defaultDir)
    props.put("host.name", "localhost")
    props.put("delete.topic.enable", "true")
    props.put("offsets.topic.replication.factor", "1")  // by default it will expect 3
    props
  }

  private def getZkPort(inetAddress: String): Int = {
    if (inetAddress == null || inetAddress.isEmpty || !inetAddress.contains(":")) {
      throw new IllegalArgumentException("The given connection address in string is invalid.")
    }
    inetAddress.split(":")(1).toInt
  }
}
