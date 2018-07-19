package io.rml.framework

import java.io.File
import java.util.concurrent.Executors

import io.rml.framework.helper.{Logger, Sanitizer, StreamTestHelper, TestSink}
import io.rml.framework.helper.fileprocessing.{DataSourceTestHelper, ExpectedOutputTestHelper}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.minicluster.LocalFlinkMiniCluster
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.AsyncFlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class StreamingTest extends AsyncFlatSpec {
  implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val cluster: Future[LocalFlinkMiniCluster] = StreamTestHelper.getClusterFuture

  "TCPsource -pull " should "map the incoming statements correctly with a valid mapping file" in {

    val folder = new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0007a-JSON")
    val folder2 = new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0007c-JSON")

    val folderLists = List(folder, folder2)
    StreamTestHelper.getTCPFuture()

    cluster map { cluster =>
      println("Cluster future started")
      val dataStream = StreamTestHelper.createDataStream(folder)
      val previous = StreamTestHelper.submitJobToCluster(cluster, dataStream, folder.getName)
      val expectedOutputs: Set[String] = ExpectedOutputTestHelper.processFilesInTestFolder(folder.toString).toSet.flatten

      /**
        * Send the input data as a stream of strings to port 9999
        */
      val chlHandler = TestUtil.getChCtxFuture
      val inputData = DataSourceTestHelper.processFilesInTestFolder(folder.toString).flatten
      StreamTestHelper.writeDataToTCP(inputData.iterator, chlHandler)


      compareResults(Sanitizer.sanitize(expectedOutputs))
    }
    println("Cluster future started")

    Thread.sleep(Long.MaxValue)

    Await.result(Future(), Duration.Inf)

    succeed
  }

  def compareResults(expectedOutputs: Set[String]) : Unit = {
    /**
    * Don't think there is an alternative way to react to end of processing the data stream
    * except waiting....
    */

    Thread.sleep(5000)
    val generatedOutputs = Sanitizer.sanitize(TestSink.getTriples.filter( !_.isEmpty))


    Logger.logInfo("Generated output: \n " + generatedOutputs.mkString("\n"))
    Logger.logInfo("Expected Output: \n " + expectedOutputs.mkString("\n"))


    /**
      * Check if the generated triple is in the expected output.
      */

    Logger.logInfo("Generated size: " + generatedOutputs.size)

    for (generatedTriple <- generatedOutputs) {

      val errorMsgMismatch = Array("Generated output does not match expected output",
        "Expected: \n" + expectedOutputs.mkString("\n"),
        "Generated: \n" + generatedOutputs.mkString("\n")).mkString("\n")


      assert(expectedOutputs.contains(generatedTriple), errorMsgMismatch)
    }
  }
}
