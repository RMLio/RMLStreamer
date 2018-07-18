package io.rml.framework

import java.io.File
import java.util.concurrent.Executors

import io.rml.framework.helper.fileprocessing.{DataSourceTestHelper, MappingTestHelper}
import io.rml.framework.helper.{Logger, TestSink}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.scalatest.AsyncFlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class StreamingTest extends AsyncFlatSpec {
  implicit val env = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val passing = "stream"

  var previous: DataStream[String] = _


  "TCPsource -pull " should "map the incoming statements correctly with a valid mapping file" in {
    val generationFuture = prepareGenerationFuture("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0007a-JSON")
    Thread.sleep(5000)

    val folder = new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0007c-JSON")
    val fRML = MappingTestHelper.processFilesInTestFolder(folder.toString)

    Main.createStreamFromFormattedMapping(fRML.head).addSink(TestSink())

    TestUtil.getChCtxFuture.foreach(ctx => {
      Thread.sleep(2000)
      val msg = DataSourceTestHelper.processFile(new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/example-data.json"))
      val byteBufMsg = ctx.alloc.buffer(msg.head.length)
      byteBufMsg.writeBytes(msg.head.getBytes)
      ctx.channel.writeAndFlush(byteBufMsg)
    })



    Await.result(generationFuture, Duration.Inf)

    succeed
  }


  /**
    * Create a combined future where TCP server is setup and setup of  flink job to generate rml mapping based on
    * the incoming messages on port 9999.
    *
    * @param testCaseFolderPath relative string path of the mapping file.
    * @return combined Future[Unit]
    */
  def prepareGenerationFuture(testCaseFolderPath: String): Future[Unit] = {
    // read the mapping
    val formattedMapping = MappingTestHelper.processFilesInTestFolder(testCaseFolderPath)
    // read data source file
    val dataSource = DataSourceTestHelper.processFilesInTestFolder(testCaseFolderPath).flatten

    Logger.logInfo(dataSource.toString())
    // execute
    previous = Main.createStreamFromFormattedMapping(formattedMapping.head)
    previous.addSink(TestSink()) //TODO write to collection for assertions

    val listenFuture = startTCPFuture(dataSource)
    val flinkFuture = startFlinkJob()

    listenFuture flatMap { _ => flinkFuture }
  }

  def startTCPFuture(messages: List[String], port: Int = 9999): Future[Unit] = Future {
    Logger.logInfo(s"Begin TCP server on port: $port ")
    TestUtil.createTCPServer(port, messages.iterator)
  }

  def startFlinkJob(): Future[Unit] = Future {
    Logger.logInfo("Started Flink job for rml generation")

    Logger.logInfo(senv.getExecutionPlan)
    senv.execute("Streaming Test")

    Logger.logInfo("finished")
  }


}
