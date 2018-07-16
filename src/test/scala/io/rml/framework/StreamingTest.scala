package io.rml.framework

import java.util.concurrent.Executors

import io.rml.framework.helper.fileprocessing.{DataSourceTestHelper, MappingTestHelper}
import io.rml.framework.helper.{Logger, TestSink}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.AsyncFlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class StreamingTest extends AsyncFlatSpec {
  implicit val env = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val passing = "stream"


  "TCPsource -pull " should "map the incoming statements correctly with a valid mapping file" in {
    val generationFuture = prepareGenerationFuture("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0001b-XML")
    Await.result(generationFuture, 5 seconds)

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

    // execute
    Main.createStreamFromFormattedMapping(formattedMapping.head).addSink(TestSink()) //TODO write to collection for assertions


    val listenFuture = listenForInput(dataSource)
    val flinkFuture = startFlinkJob()

    listenFuture flatMap { _ => flinkFuture }
  }

  def listenForInput( messages: List[String],port: Int = 9999): Future[Unit] = Future {
    Logger.logInfo(s"Begin TCP server on port: $port ")
    TestUtil.createTCPServer(port, messages.iterator)

  }

  def startFlinkJob(): Future[Unit] = Future {
    Logger.logInfo("Started Flink job for rml generation")
    Thread.sleep(3000) // wait for TCP server to be set up first
    senv.execute("Streaming Test")

  }



}
