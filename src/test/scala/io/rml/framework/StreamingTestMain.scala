package io.rml.framework

import java.io.{File, StringReader}
import java.util.concurrent.Executors

import io.rml.framework.engine.PostProcessor
import io.rml.framework.shared.RMLException
import io.rml.framework.util._
import io.rml.framework.util.fileprocessing.{ExpectedOutputTestUtil, MappingTestUtil, StreamDataSourceTestUtil}
import io.rml.framework.util.logging.Logger
import io.rml.framework.util.server._
import org.apache.flink.api.common.JobID
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.minicluster.MiniCluster
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object StreamingTestMain {


  def main(args: Array[String]): Unit = {
    Logger.lineBreak(50)
    implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    implicit val cluster: Future[MiniCluster] = StreamTestUtil.getClusterFuture("main")
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
    val server = serverFactoryMap(testType).createServer("main")
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
        TestUtil.tmpCleanup("main")

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
      val sink = TestSink2()
      Logger.logInfo("sink created")
      dataStream.addSink(sink)

      Logger.logInfo("Sink added")
      val eventualJobID = StreamTestUtil.submitJobToCluster(cluster, dataStream, folder.getName)
      Await.result(eventualJobID, Duration.Inf)
      val jobID = eventualJobID.value.get.get
      Logger.logInfo(s"Cluster job $jobID started")


      val inputData = StreamDataSourceTestUtil.processFilesInTestFolder(folder.toString)

      Logger.logInfo(s"Start reading input data")

      Logger.logInfo(inputData.toString())
      server.writeData(inputData)

      Logger.logInfo("Input Data sent to server")

      // if we don't expect output, don't wait too long
      val expectedOutput = getExpectedOutputs(folder)
      var counter = if (expectedOutput.isEmpty) 10 else 100

      while (TestSink2.getTriples().isEmpty && counter > 0) {
        Thread.sleep(300)
        counter -= 1
        if (counter % 10 == 0) {
          Logger.logInfo("Waiting for output from the streamer...")
        }
      }
      Thread.sleep(300)


      val resultTriples = TestSink2.getTriples()
      Logger.logInfo(s"Test got a result of ${resultTriples.length} triple(s)")

      val either = StreamingTestMain.compareResults(folder, expectedOutput, resultTriples)
      either match {
        case Left(e) => Future.failed(new RMLException(e))
        case Right(e) => Future.successful()
      }
    }
  }
  def resetTestStates(jobID: JobID, cluster: MiniCluster)(implicit executionContextExecutor: ExecutionContextExecutor): Future[Unit] = {

    // Cancel the job
    StreamingTestMain.synchronized {
      Future {
        StreamTestUtil.cancelJob(jobID, cluster)
        while(!cluster.getJobStatus(jobID).get().isTerminalState){
          Thread.sleep(100)
        }
      }
    }
  }

  def compareResults(folder: File, expectedOutputs: Set[String], unsanitizedOutput: List[String]): Either[String,String] = {
    val generatedOutputs = Sanitizer.sanitize(unsanitizedOutput)

    if (expectedOutputs nonEmpty) {
      val expectedStr = expectedOutputs.mkString("\n")
      val generatedStr = generatedOutputs.mkString("\n")

      Logger.logInfo(List("Generated output: ", generatedStr).mkString("\n"))
      Logger.logInfo(List("Expected Output: ", expectedStr).mkString("\n"))

      val expectedModel = ModelFactory.createDefaultModel()
      try {
        expectedModel.read(new StringReader(expectedStr), "base", Lang.NQUADS.getName)
      } catch {
        case _ => expectedModel.read(new StringReader(expectedStr), "base", Lang.JSONLD.getName)
      }

      val generatedModel = ModelFactory.createDefaultModel()
      try {
        generatedModel.read(new StringReader(generatedStr), "base", Lang.NQUADS.getName)
      } catch {
        case _ => generatedModel.read(new StringReader(expectedStr), "base", Lang.JSONLD.getName)
      }

      if (generatedModel.isIsomorphicWith(expectedModel)) {
        Right(s"Testcase ${folder.getName} passed streaming test!")
      } else {
        Left(s"Generated output does not match expected output:\nExpected:\n${expectedStr}\nGenerated:\n${generatedStr}\n")
      }
    } else {
      Right(s"Testcase ${folder.getName} passed streaming test!")
    }
  }

  def getExpectedOutputs(folder: File): Set[String] = {
    var expectedOutputs: Set[String] = ExpectedOutputTestUtil.processFilesInTestFolder(folder.toString).toSet.flatten
    Sanitizer.sanitize(expectedOutputs)
  }
}
