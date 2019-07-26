package io.rml.framework

import java.util.concurrent.Executors

import io.rml.framework.util.StreamTestUtil
import io.rml.framework.util.server.{KafkaTestServerFactory, TestServer}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.minicluster.MiniCluster
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class KafkaStreamingTest extends StaticTestSpec with ReadMappingBehaviour{
  implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  val cluster: Future[MiniCluster] = StreamTestUtil.getClusterFuture
  var serverOpt:TestServer = KafkaTestServerFactory.createServer()

  val passing = Array(("stream/kafka","noopt"))

  "A streamer mapping reader" should behave like validMappingFile("stream/kafka")

  it should behave like invalidMappingFile("negative_test_cases")


}
