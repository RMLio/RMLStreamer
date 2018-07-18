package io.rml.framework

import java.io.File
import java.util.concurrent.Executors

import io.rml.framework.helper.fileprocessing.{DataSourceTestHelper, MappingTestHelper}
import io.rml.framework.helper.{Logger, StreamTestHelper, TestSink}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.{ConfigConstants, Configuration, TaskManagerOptions}
import org.apache.flink.runtime.messages.JobManagerMessages.CancelJob
import org.apache.flink.runtime.minicluster.LocalFlinkMiniCluster
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.scalatest.AsyncFlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class StreamingTest extends AsyncFlatSpec {

  implicit val env = ExecutionEnvironment.getExecutionEnvironment
  implicit var senv = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val passing = "stream"
  var cluster: LocalFlinkMiniCluster = _
  var previous: DataStream[String] = _


  "TCPsource -pull " should "map the incoming statements correctly with a valid mapping file" in {

    val folder = new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0007a-JSON")
    val folder2 = new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0007c-JSON")
    prepareGenerationFuture(folder.toString)

    StreamTestHelper.getClusterFuture
    Logger.logInfo("cluster started")


    Thread.sleep(3000)
    createDataStream(folder)
    val graph = senv.getStreamGraph
    graph.setJobName(folder.getName)
    val jobGraph = graph.getJobGraph
    cluster.submitJobDetached(jobGraph)
    val previous = jobGraph.getJobID
    Logger.logInfo("jobs: " + cluster.currentlyRunningJobs)
    Logger.logInfo("first started")


    Logger.logInfo("Jobmanagers: " + cluster.numJobManagers)

    Thread.sleep(5000)

    val mnger = cluster.getLeaderGatewayFuture

    mnger foreach { gateway =>
      gateway.ask(new CancelJob(previous), FiniteDuration(1, "seconds"))
      createDataStream(folder2)
      val graph2 = senv.getStreamGraph
      graph2.setJobName(folder2.getName)
      cluster.submitJobDetached(graph2.getJobGraph)
      Logger.logInfo("second started")


      val sauce = DataSourceTestHelper.processFilesInTestFolder(folder2.toString).flatten
      Thread.sleep(5000)
      TestUtil.getChCtxFuture.foreach(ctx => {

        for (el <- sauce) {
          val byteBuff = ctx.alloc.buffer(el.length)
          byteBuff.writeBytes(el.getBytes())
          ctx.channel.writeAndFlush(byteBuff)
        }
      })


      Logger.logInfo(cluster.currentlyRunningJobs.toString())
    }
    Thread.sleep(Long.MaxValue)

    Await.result(Future(), Duration.Inf)

    succeed
  }



  def createDataStream(testCaseFolder: File): DataStream[String] = {
    senv = StreamExecutionEnvironment.createLocalEnvironment()
    // read the mapping
    val formattedMapping = MappingTestHelper.processFilesInTestFolder(testCaseFolder.getAbsolutePath)
    val stream = Main.createStreamFromFormattedMapping(formattedMapping.head)


    stream.addSink(TestSink())
    stream
  }


  /**
    * Create a combined future where TCP server is setup and setup of  flink job to generate rml mapping based on
    * the incoming messages on port 9999.
    *
    * @param testCaseFolderPath relative string path of the mapping file.
    * @return combined Future[Unit]
    */
  def prepareGenerationFuture(testCaseFolderPath: String): Future[Unit] = {
    // read data source file
    val dataSource = DataSourceTestHelper.processFilesInTestFolder(testCaseFolderPath).flatten

    startTCPFuture(dataSource)
  }

  def startTCPFuture(messages: List[String], port: Int = 9999): Future[Unit] = Future {
    Logger.logInfo(s"Begin TCP server on port: $port ")
    TestUtil.createTCPServer(port, messages.iterator)
  }


}
