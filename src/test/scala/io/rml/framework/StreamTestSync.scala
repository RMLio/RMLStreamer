package io.rml.framework

import java.io.{File, StringReader}
import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors

import io.rml.framework.core.internal.Logging
import io.rml.framework.engine.PostProcessor
import io.rml.framework.util.fileprocessing.{ExpectedOutputTestUtil, MappingTestUtil, StreamDataSourceTestUtil}
import io.rml.framework.util.logging.Logger
import io.rml.framework.util.server.{TestData, TestSink2}
import io.rml.framework.util.{Sanitizer, StreamTestUtil, TestUtil}
import org.apache.flink.api.common.JobID
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.jobgraph.{JobGraph, JobStatus}
import org.apache.flink.runtime.minicluster.{MiniCluster, MiniClusterConfiguration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.reflect.io.Directory

/**
  * <p>Copyright 2019 IDLab (Ghent University - imec)</p>
  *
  * @author Gerald Haesendonck
  */
abstract class StreamTestSync extends StaticTestSpec with ReadMappingBehaviour with Logging {

  protected def testFolder: String

  // tuples (folder of test data, post processor to use)
  protected def passingTests: Array[(String, String)]

  // set up things necessary before running actuaol tests
  def setup(): Unit = {
    val tmpDir = getTempDir
    if (tmpDir.exists) {
      logInfo(s"Found tmp dir ${tmpDir}. Deleting it.")
      val dir = new Directory(tmpDir)
      if (!dir.deleteRecursively) {
        logWarning(s"Could not delete tmp dir ${tmpDir}")
      }
    }
  }

  // Things to do before running one test case
  protected def beforeTestCase(): Unit

  // Things to do after running one test case
  protected def afterTestCase(): Unit

  // tear down
  protected def teardown(): Unit

  // write data to flink via subclass (Kafka, TCP, ...)
  protected def writeData(input: List[TestData])(implicit executor: ExecutionContextExecutor)

  // turn the folder + post processor data into test cases
  logInfo(s"==== Starting ${this.getClass.getSimpleName} ====")

  // check mapping files
  "A streamer mapping reader" should behave like validMappingFile(testFolder)

  // run setup of subclass
  setup()

  // set up Flink
  val flink: MiniCluster = startFlink

    logInfo("Reading test cases")
  val testCases: Array[(Path, String)] = for {
    (folder, postProcessor) <- passingTests
    testCase <- StreamDataSourceTestUtil.getTestCaseFolders(folder).sorted
  } yield (testCase, postProcessor)

  // run the test cases
  for ((folderPath, postProcessorName) <- testCases) {

    //it should s"produce triples equal to the expected triples for ${folderPath.getFileName}" in {
      Logger.lineBreak(50)
      logInfo(s"Running test ${folderPath}")
      beforeTestCase()

      implicit val postProcessor: PostProcessor = TestUtil.pickPostProcessor(postProcessorName)
      val folder = MappingTestUtil.getFile(folderPath.toString)

      // set up the execution environments
      implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
      implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
      implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

      // create data stream and sink
      val dataStream = StreamTestUtil.createDataStream(folder)
      Logger.logInfo("Datastream created")
      val sink = TestSink2()
      Logger.logInfo("sink created")
      dataStream.addSink(sink)
      Logger.logInfo("Sink added")

      // submit job to Flink
      val jobId = submitJob(flink, dataStream, folder.getName)

      // read input data and send via server (Kafka, TCP, ...) to Flink
      val inputData = StreamDataSourceTestUtil.processFilesInTestFolder(folder.toString)
      logInfo("Sending input data...")
      writeData(inputData)
      logInfo("Input data sent.")

      // see what output we expect and wait for Flink to get that output
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

      // delete Flink Job
      deleteJob(flink, jobId)

      afterTestCase()

      // check the results
      val either = compareResults(folder, expectedOutput, resultTriples)

    it should s"produce triples equal to the expected triples for ${folderPath.getFileName}" in {
      either match {
        case Left(e) => fail(e)
        case Right(e) => {
          logInfo(e)
          succeed
        }
      }
    }
    //}
  }

  // run teardown of subclass
  teardown()

  /////////////////////////
  // some helper methods //
  /////////////////////////
  protected def getTempDir: File = {
    val file = Paths.get(System.getProperty("java.io.tmpdir"), "rml-streamer", this.getClass.getSimpleName).toFile
    if (!file.exists()) {
      file.mkdir()
    }
    logInfo(s"Temp folder: ${file.toString}")
    file
  }

  override def logInfo(log: String) {
    Logger.logInfo(s"${getClass.getSimpleName}: ${log}")
  }

  private def startFlink: MiniCluster = {
    logInfo("Starting Flink...")
    val customConfig = new Configuration()
    customConfig.setString("io.tmp.dirs", getTempDir.getAbsolutePath)
    customConfig.setString("rest.bind-port", "50000-51000") // see https://github.com/apache/flink/commit/730eed71ef3f718d61f85d5e94b1060844ca56db
    val configuration = new MiniClusterConfiguration.Builder()
      .setConfiguration(customConfig)
      .setNumTaskManagers(1)
      .setNumSlotsPerTaskManager(100)
      .build()
    // start cluster
    val flink = new MiniCluster(configuration)
    flink.start()
    logInfo("Flink started.")
    flink
  }

  private def submitJob[T](flink: MiniCluster, dataStream: DataStream[T], name: String): JobID = {
    logInfo(s"Submitting job ${name} to Flink...")
    val graph = dataStream.executionEnvironment.getStreamGraph
    graph.setJobName(name)
    val jobGraph: JobGraph = graph.getJobGraph
    flink.runDetached(jobGraph)
    val jobId = jobGraph.getJobID
    logInfo(s"Job submitted, ID: ${jobId}. Waiting for it to run.")

    while (!flink.getJobStatus(jobId).get().equals(JobStatus.RUNNING)) {
      Thread.sleep(1000)
      logInfo("Waiting for Flink job to start...")
    }
    Thread.sleep(1000)
    logInfo("Flink job started.")
    jobId
  }

  private def deleteJob(fink: MiniCluster, jobId: JobID): Unit = {
    logInfo(s"Canceling job ${jobId}...")
    flink.cancelJob(jobId).get()
    Thread.sleep(1000)  // also here: even waiting for the future to complete doesn't guarantee that it's completed!
    logInfo(s"Job ${jobId} canceled.")
  }

  private def getExpectedOutputs(folder: File): Set[String] = {
    val expectedOutputs: Set[String] = ExpectedOutputTestUtil.processFilesInTestFolder(folder.toString).toSet.flatten
    Sanitizer.sanitize(expectedOutputs)
  }

  private def compareResults(folder: File, expectedOutputs: Set[String], unsanitizedOutput: List[String]): Either[String,String] = {
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
        case _: Throwable => expectedModel.read(new StringReader(expectedStr), "base", Lang.JSONLD.getName)
      }

      val generatedModel = ModelFactory.createDefaultModel()
      try {
        generatedModel.read(new StringReader(generatedStr), "base", Lang.NQUADS.getName)
      } catch {
        case _: Throwable => generatedModel.read(new StringReader(expectedStr), "base", Lang.JSONLD.getName)
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
}
