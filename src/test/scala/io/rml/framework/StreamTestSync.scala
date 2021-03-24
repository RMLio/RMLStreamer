/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/
package io.rml.framework

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.extractors.TriplesMapsCache
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.util.{StreamerConfig, Util}
import io.rml.framework.engine.PostProcessor
import io.rml.framework.util.fileprocessing.StreamDataSourceTestUtil
import io.rml.framework.util.logging.Logger
import io.rml.framework.util.server.{TestData, TestSink2}
import io.rml.framework.util.{StreamTestUtil, TestUtil}
import org.apache.flink.api.common.{JobID, JobStatus}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.jobgraph.JobGraph
import org.apache.flink.runtime.minicluster.{MiniCluster, MiniClusterConfiguration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import java.io.File
import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.reflect.io.Directory

/**
  * @author Gerald Haesendonck
  */
abstract class StreamTestSync extends StaticTestSpec with ReadMappingBehaviour with Logging {

  // first we set the environment right
  RMLEnvironment.setGeneratorBaseIRI(Some("http://example.com/base/"))

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
    StreamerConfig.setExecuteLocalParallel(true)
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
    TriplesMapsCache.clear();

    //it should s"produce triples equal to the expected triples for ${folderPath.getFileName}" in {
    Logger.lineBreak(50)
    logInfo(s"Running test ${folderPath}")
    beforeTestCase()

    implicit val postProcessor: PostProcessor = TestUtil.pickPostProcessor(postProcessorName)
    val folder = Util.getFile(folderPath.toString)

    // set up the execution environments
    implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI()

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
    val (expectedOutput, expectedOutputFormat) = TestUtil.getExpectedOutputs(folder)
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
    val either = TestUtil.compareResults(folderPath.toString, resultTriples, expectedOutput, postProcessor.outputFormat, expectedOutputFormat)
    either match {
      case Left(e) => fail(e)
      case Right(e) => {
        logInfo(e)
      }
    }

    it should s"produce triples equal to the expected triples for ${folderPath.toString}" in {
      either match {
        case Left(e) => fail(e)
        case Right(e) => {
          logInfo(e)
          succeed
        }
      }
    }
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
    customConfig.setString("classloader.check-leaked-classloader", "false") // this option is to required to fix strange issue related to class loading, see discussion: https://gitlab.ilabt.imec.be/rml/proc/rml-streamer/-/issues/121

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

  private def deleteJob(flink: MiniCluster, jobId: JobID): Unit = {
    logInfo(s"Canceling job ${jobId}...")
    flink.cancelJob(jobId).get()
    Thread.sleep(1000)  // also here: even waiting for the future to complete doesn't guarantee that it's completed!
    logInfo(s"Job ${jobId} canceled.")
  }
}
