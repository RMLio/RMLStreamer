package io.rml.framework

import java.io.File
import java.util.concurrent.Executors

import io.rml.framework.util.{Logger, _}
import io.rml.framework.util.fileprocessing.{DataSourceTestUtil, ExpectedOutputTestUtil, MappingTestUtil}
import org.apache.flink.api.common.JobID
import org.apache.flink.api.java.utils.ParameterTool
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


object StreamingTest {
  implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val cluster: Future[LocalFlinkMiniCluster] = StreamTestUtil.getClusterFuture

  def main(args: Array[String]): Unit = {

    val EMPTY_VALUE = "__NO_VALUE_KEY"

    // get parameters
    if(args.nonEmpty)
      Logger.logInfo(s"Arguments: ${args.mkString(" ")}")

    val parameters = ParameterTool.fromArgs(args)

    val fileName = if (parameters.has("path")) parameters.get("path")
    else "stream/csv/RMLTC0012a-CSV"


    val folder = MappingTestUtil.getFile(fileName)

    StreamTestUtil.getTCPFuture()


    Await.result(executeTestCase(folder), Duration.Inf)
    sys.exit(1)

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
        Await.result(resetTestStates(jobID, cluster), Duration.Inf)
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

  def compareResults(folder: File): Unit = {

    Thread.sleep(6000)

    var expectedOutputs: Set[String] = ExpectedOutputTestUtil.processFilesInTestFolder(folder.toString).toSet.flatten
    expectedOutputs = Sanitizer.sanitize(expectedOutputs)
    val generatedOutputs = Sanitizer.sanitize(TestSink.getTriples.filter(!_.isEmpty))


    Logger.logInfo(List("Generated output: ", generatedOutputs.mkString("\n")).mkString("\n"))
    Logger.logInfo(List("Expected Output: ", expectedOutputs.mkString("\n")).mkString("\n"))


    /**
      * Check if the generated triple is in the expected output.
      */

    Logger.logInfo("Generated size: " + generatedOutputs.size)

    val errorMsgMismatch = Array("Generated output does not match expected output",
      "Expected: ", expectedOutputs.mkString("\n"),
      "Generated: ", generatedOutputs.mkString("\n"),
      s"Test case: ${folder.getName}").mkString("\n")


    if (expectedOutputs.nonEmpty && expectedOutputs.size  > generatedOutputs.size  ) {
      errorMsgMismatch.split("\n").foreach(Logger.logError)
      return
    }

    for (generatedTriple <- generatedOutputs) {
      if(!expectedOutputs.contains(generatedTriple)){
        errorMsgMismatch.split("\n").foreach(Logger.logError)
        return
      }
    }

    Logger.logSuccess(s"Testcase ${folder.getName} passed streaming test!")
  }
}
