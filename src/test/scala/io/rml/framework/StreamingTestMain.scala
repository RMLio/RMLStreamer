package io.rml.framework

import java.io.File
import java.util.concurrent.{CompletableFuture, Executors}

import io.rml.framework.engine.{BulkPostProcessor, JsonLDProcessor, NopPostProcessor, PostProcessor}
import io.rml.framework.util.{Logger, _}
import io.rml.framework.util.fileprocessing.{DataSourceTestUtil, ExpectedOutputTestUtil, MappingTestUtil}
import io.rml.framework.util.server.{TCPTestServer, TestServer}
import org.apache.flink.api.common.JobID
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.messages.Acknowledge
import org.apache.flink.runtime.minicluster.MiniCluster
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.AsyncFlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}


object StreamingTestMain {
  Logger.lineBreak(50)
  implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val cluster: Future[MiniCluster] = StreamTestUtil.getClusterFuture
  var serverOpt:Option[TestServer] =  None

  val serverFactoryMap: Map[String,StreamTestServerFactory] =  Map("tcp" -> TCPTestServerFactory, "kafka" -> KafkaTestServerFactory)

  val PATH_PARAM = "path"
  val TYPE_PARAM =  "type"
  val POST_PROCESS_PARAM = "post-process"


  def main(args: Array[String]): Unit = {

    val EMPTY_VALUE = "__NO_VALUE_KEY"

    // get parameters
    if(args.nonEmpty)
      Logger.logInfo(s"Arguments: ${args.mkString(" ")}")

    val parameters = ParameterTool.fromArgs(args)

    val fileName = if (parameters.has(PATH_PARAM)) parameters.get(PATH_PARAM)
    else "json-ld/stream/tcp/RMLTC0012a-XML-STREAM-SPLIT"

    val testType = if (parameters.has(TYPE_PARAM)) parameters.get(TYPE_PARAM)
    else "tcp"

    val postProcessorType = if(parameters.has(POST_PROCESS_PARAM)) parameters.get(POST_PROCESS_PARAM)
    else "json-ld"

    implicit val postProcessor:PostProcessor = TestUtil.pickPostProcessor(postProcessorType)

    val folder = MappingTestUtil.getFile(fileName)
    val server = serverFactoryMap(testType).createServer()
    serverOpt =  Some(server)
    server.setup()


    Await.result(executeTestCase(folder), Duration.Inf)

    server.tearDown()
    Logger.lineBreak(50)
    sys.exit(1)

  }


  def executeTestCase(folder: File)(implicit postProcessor: PostProcessor): Future[Unit] = {
    cluster flatMap { cluster =>
      cluster.synchronized {

        if(serverOpt.isEmpty){
          throw new IllegalStateException("Set up the server first!!!")
        }
        Logger.logInfo(folder.toString)
        val dataStream = StreamTestUtil.createDataStream(folder)
        val eventualJobID = StreamTestUtil.submitJobToCluster(cluster, dataStream, folder.getName)
        Await.result(eventualJobID, Duration.Inf)
        val jobID = eventualJobID.value.get.get
        Logger.logInfo(s"Cluster job $jobID started")

        /**
          * Send the input data as a stream of strings to port 9999
          */
        val inputData = DataSourceTestUtil.processFilesInTestFolder(folder.toString).flatten

        serverOpt.get.writeData(inputData)
        Thread.sleep(6000)


        StreamingTestMain.compareResults(folder,  TestSink.getTriples.filter(!_.isEmpty))
        val waitfor = resetTestStates(jobID, cluster)
        waitfor.get
        //Await.result(resetTestStates(jobID, cluster), Duration.Inf)
        Future.successful(s"Cluster job $jobID done")
      }
    }
  }

  def resetTestStates(jobID: JobID, cluster: MiniCluster): CompletableFuture[Acknowledge] = {
    // Clear the collected results in the sink
    TestSink.empty()
    // Cancel the job
    StreamTestUtil.cancelJob(jobID, cluster)
  }

  def compareResults(folder: File, unsanitizedOutput: List[String]): Unit = {


    var expectedOutputs: Set[String] = ExpectedOutputTestUtil.processFilesInTestFolder(folder.toString).toSet.flatten
    expectedOutputs = Sanitizer.sanitize(expectedOutputs)
    val generatedOutputs = Sanitizer.sanitize(unsanitizedOutput)


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
