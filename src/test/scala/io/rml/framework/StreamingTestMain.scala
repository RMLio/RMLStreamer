package io.rml.framework

import java.io.File
import java.util.concurrent.{CompletableFuture, Executors, TimeUnit}

import io.rml.framework.engine.PostProcessor
import io.rml.framework.shared.RMLException
import io.rml.framework.util._
import io.rml.framework.util.fileprocessing.{ExpectedOutputTestUtil, MappingTestUtil, StreamDataSourceTestUtil}
import io.rml.framework.util.logging.Logger
import io.rml.framework.util.server._
import org.apache.flink.api.common.JobID
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.messages.Acknowledge
import org.apache.flink.runtime.minicluster.MiniCluster
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object StreamingTestMain {


  def main(args: Array[String]): Unit = {
    Logger.lineBreak(50)
    implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    implicit val cluster: Future[MiniCluster] = StreamTestUtil.getClusterFuture
    var serverOpt: Option[TestServer] = None

    val serverFactoryMap: Map[String, StreamTestServerFactory] = Map("tcp" -> TCPTestServerFactory, "kafka" -> KafkaTestServerFactory)

    val PATH_PARAM = "path"
    val TYPE_PARAM = "type"
    val POST_PROCESS_PARAM = "post-process"

    val EMPTY_VALUE = "__NO_VALUE_KEY"

    // get parameters
    if (args.nonEmpty)
      Logger.logInfo(s"Arguments: ${args.mkString(" ")}")

    val parameters = ParameterTool.fromArgs(args)

    val fileName = if (parameters.has(PATH_PARAM)) parameters.get(PATH_PARAM)
    else "stream/kafka/RMLTC0007e-XML-STREAM-KAFKA"
    val testType = if (parameters.has(TYPE_PARAM)) parameters.get(TYPE_PARAM)
    else "kafka"

    val postProcessorType = if (parameters.has(POST_PROCESS_PARAM)) parameters.get(POST_PROCESS_PARAM)
    else "noopt"

    implicit val postProcessor: PostProcessor = TestUtil.pickPostProcessor(postProcessorType)

    val folder = MappingTestUtil.getFile(fileName)
    Logger.logInfo(s"Creating $testType server")
    val server = serverFactoryMap(testType).createServer()
    serverOpt = Some(server)
    server.setup()

    Logger.logInfo("Server setup done")
    val awaited = Await.ready(executeTestCase(folder, server), Duration.Inf)

    awaited andThen {
      case Success(_) =>
        Logger.logSuccess(s"Test passed!!")
    } andThen {
      case Failure(exception) =>
        Logger.logError(exception.toString)

    } andThen {
      case _ =>
        server.tearDown()
        TestUtil.tmpCleanup()

        Logger.lineBreak(50)
        sys.exit(1)
    }
  }


  def executeTestCase(folder: File, server: TestServer)(implicit postProcessor: PostProcessor,
                                                        cluster: Future[MiniCluster],
                                                        executionContextExecutor: ExecutionContextExecutor,
                                                        streamExecutionEnvironment: StreamExecutionEnvironment,
                                                        executionEnvironment: ExecutionEnvironment): Future[Unit] = {
    cluster flatMap { cluster =>
      if (server == null) {
        throw new IllegalStateException("Set up the server first!!!")
      }
      Logger.logInfo(folder.toString)
      val dataStream = StreamTestUtil.createDataStream(folder)


      Logger.logInfo("Datastream created")
      val sink = TestSink()
      val expectedOutput = ExpectedOutputTestUtil.processFilesInTestFolder(folder.toString).toSet.flatten
      TestSink.setExpectedTriples(Sanitizer.sanitize(expectedOutput))
      Logger.logInfo("sink created")
      dataStream.addSink(sink)

      Logger.logInfo("Sink added")
      val eventualJobID = StreamTestUtil.submitJobToCluster(cluster, dataStream, folder.getName)
      Await.result(eventualJobID, Duration.Inf)
      val jobID = eventualJobID.value.get.get
      Logger.logInfo(s"Cluster job $jobID started")


      val inputData = StreamDataSourceTestUtil.processFilesInTestFolder(folder.toString)

      Logger.logInfo(s"Start reading input data")
      TestSink.startCountDown(10 second)

      Logger.logInfo(inputData.toString())
      server.writeData(inputData)

      Logger.logInfo("Input Data sent to server")


      TestSink.sinkFuture flatMap {
        _ =>
          Logger.logInfo(s"Sink's promise completion status: ${TestSink.sinkPromise.isCompleted}")
          val either = StreamingTestMain.compareResults(folder, TestSink.getTriples.filter(!_.isEmpty))
          TestSink.reset()
          either match {
            case Left(e) => Future.failed(new RMLException(e))
            case Right(e) => Future.successful()
          }
      } flatMap {
        _ =>
        //Await.result(resetTestStates(jobID, cluster), Duration.Inf)
        Logger.logInfo(s"Cluster job $jobID done")
        resetTestStates(jobID, cluster)
      }
    }
  }

  def resetTestStates(jobID: JobID, cluster: MiniCluster)(implicit executionContextExecutor: ExecutionContextExecutor): Future[Unit] = {

    // Cancel the job
    StreamingTestMain.synchronized {
      Future {
        StreamTestUtil.cancelJob(jobID, cluster)
        while(!cluster.getJobStatus(jobID).get().isTerminalState){
        }
      }
    }
  }

  def compareResults(folder: File, unsanitizedOutput: List[String]): Either[String,String] = {

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


    if (expectedOutputs.nonEmpty && expectedOutputs.size > generatedOutputs.size) {
      errorMsgMismatch.split("\n").foreach(Logger.logError)
      return Left(errorMsgMismatch)
    }

    for (generatedTriple <- generatedOutputs) {
      if (!expectedOutputs.contains(generatedTriple)) {
        errorMsgMismatch.split("\n").foreach(Logger.logError)
        return Left(errorMsgMismatch)
      }
    }

    Right(s"Testcase ${folder.getName} passed streaming test!")
  }
}
