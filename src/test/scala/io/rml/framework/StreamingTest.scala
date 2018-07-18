package io.rml.framework

import java.io.File
import java.util.concurrent.Executors

import io.rml.framework.helper.{Logger, StreamTestHelper}
import io.rml.framework.helper.fileprocessing.DataSourceTestHelper
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
  val cluster: Future[LocalFlinkMiniCluster] =  StreamTestHelper.getClusterFuture

  "TCPsource -pull " should "map the incoming statements correctly with a valid mapping file" in {

    val folder = new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0007a-JSON")
    val folder2 = new File("/home/sitt/Documents/idlab/rml-streamer/src/test/resources/stream/RMLTC0007c-JSON")


    StreamTestHelper.getTCPFuture()

    cluster map { cluster =>
      println("Cluster future started")
      val dataStream = StreamTestHelper.createDataStream(folder)
      val previous = StreamTestHelper.submitJobToCluster(cluster, dataStream, folder.getName)
      val chlHandler = TestUtil.getChCtxFuture
      val inputData = DataSourceTestHelper.processFilesInTestFolder(folder.toString).flatten
      StreamTestHelper.writeDataToTCP(inputData.iterator, chlHandler)
    }
    println("Cluster future started")

    Thread.sleep(Long.MaxValue)

    Await.result(Future(), Duration.Inf)

    succeed
  }


}
