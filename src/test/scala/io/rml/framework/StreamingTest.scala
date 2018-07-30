package io.rml.framework

import java.io.File
import java.util.concurrent.Executors

import io.rml.framework.util.{Logger, _}
import io.rml.framework.util.fileprocessing.{DataSourceTestUtil, ExpectedOutputTestUtil}
import org.apache.flink.api.common.JobID
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.minicluster.{FlinkMiniCluster, LocalFlinkMiniCluster}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.AsyncFlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

/**
  * PLEASE REIMPLEMENT THIS USING ACTOR MODELS
  *
  * RIGHT NOW  YOU HAVE TO EXECUTE THIS MANUALLY BY PROVIDING A SINGLE TEST CASE FOLDER EVERYTIME
  */


class StreamingTest extends AsyncFlatSpec {
  implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val cluster: Future[LocalFlinkMiniCluster] = StreamTestUtil.getClusterFuture


  "TCPsource -pull " should "map the incoming statements correctly with a valid mapping file" in {

    val folder = new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC1001-JSON-STREAM")

    //folderLists = List(folder).toArray
    StreamTestUtil.getTCPFuture()

    //TODO: use this when actor models are implemented
//     var folderLists = ExpectedOutputTestUtil.getTestCaseFolders("stream").map( _.toFile).sorted
//        val fSerialized = {
//          var fAccum = Future{()}
//          for (folder <- folderLists){
//            Logger.logInfo(folder.toString)
//            fAccum  =  fAccum flatMap { _ => executeTestCase(folder)}
//          }
//          fAccum
//        }

    Await.result(executeTestCase(folder), Duration.Inf)

    succeed
  }



  def executeTestCase(folder: File): Future[Unit] = {
    cluster flatMap { cluster =>
      cluster.synchronized {
        Logger.logInfo(folder.toString)
        val dataStream = StreamTestUtil.createDataStream(folder)
        val eventualJobID = StreamTestUtil.submitJobToCluster(cluster, dataStream, folder.getName)
        Await.result(eventualJobID, Duration.Inf)
        val jobID = eventualJobID.value.get.get
        Logger.logInfo(s"Cluster job $jobID started")

        /**
          * Send the input data as a stream of strings to port 9999
          */
        val chlHandler = TCPUtil.getChCtxFuture
        val inputData = DataSourceTestUtil.processFilesInTestFolder(folder.toString).flatten
        StreamTestUtil.writeDataToTCP(inputData.iterator, chlHandler)


        StreamingTest.compareResults(folder)
        Await.result(resetTestStates(jobID, cluster),Duration.Inf)
        Future.successful(s"Cluster job $jobID done")
      }
    }
  }

  def resetTestStates(jobID: JobID, cluster: FlinkMiniCluster): Future[AnyRef] = {
    // Clear the collected results in the sink
    TestSink.empty()
    // Cancel the job
    StreamTestUtil.cancelJob(jobID, cluster)
  }

}

object StreamingTest{
  def compareResults(folder: File): Unit = {

    Thread.sleep(6000)

    var expectedOutputs: Set[String] = ExpectedOutputTestUtil.processFilesInTestFolder(folder.toString).toSet.flatten
    expectedOutputs = Sanitizer.sanitize(expectedOutputs)
    val generatedOutputs = Sanitizer.sanitize(TestSink.getTriples.filter(!_.isEmpty))


    Logger.logInfo("Generated output: \n " + generatedOutputs.mkString("\n"))
    Logger.logInfo("Expected Output: \n " + expectedOutputs.mkString("\n"))


    /**
      * Check if the generated triple is in the expected output.
      */

    Logger.logInfo("Generated size: " + generatedOutputs.size)

    val errorMsgMismatch = Array("Generated output does not match expected output",
      "Expected: \n" + expectedOutputs.mkString("\n"),
      "Generated: \n" + generatedOutputs.mkString("\n"),
      s"Test case: ${folder.getName}").mkString("\n")

    if(expectedOutputs.nonEmpty){
      assert(expectedOutputs.size <= generatedOutputs.size,  errorMsgMismatch)
    }

    for (generatedTriple <- generatedOutputs) {


      assert(expectedOutputs.contains(generatedTriple), errorMsgMismatch)
    }
  }
}
