package io.rml.framework

import java.util.concurrent.Executors

import io.rml.framework.util.StreamTestUtil
import io.rml.framework.util.fileprocessing.StreamDataSourceTestUtil
import io.rml.framework.util.server.{KafkaTestServerFactory, TestServer}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.minicluster.MiniCluster
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class KafkaStreamingTest extends StaticTestSpec with ReadMappingBehaviour {
  implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val cluster: Future[MiniCluster] = StreamTestUtil.getClusterFuture
  var server: TestServer = KafkaTestServerFactory.createServer()
  server.setup()

  val passing = Array(("stream/kafka", "noopt"))
  val testCases = for {
    (folder, postProcessor) <- passing
    testCase <- StreamDataSourceTestUtil.getTestCaseFolders(folder)
  } yield (testCase.toString, postProcessor)

  "A streamer mapping reader" should behave like validMappingFile("stream/kafka")

  it should behave like invalidMappingFile("negative_test_cases")


  it should "produce triples equal to the expected triples" in {

  }


}
