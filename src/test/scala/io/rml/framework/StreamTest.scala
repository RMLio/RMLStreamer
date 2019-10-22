package io.rml.framework

import java.nio.file.Path
import java.util.concurrent.Executors

import io.rml.framework.engine.PostProcessor
import io.rml.framework.util.fileprocessing.{MappingTestUtil, StreamDataSourceTestUtil}
import io.rml.framework.util.logging.Logger
import io.rml.framework.util.server.{KafkaTestServerFactory, TCPTestServerFactory, TestServer}
import io.rml.framework.util.{StreamTestUtil, TestUtil}
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.runtime.minicluster.MiniCluster
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


/**
  * <p>Copyright 2019 IDLab (Ghent University - imec)</p>
  *
  * @author Gerald Haesendonck
  */
abstract class StreamTest(val streamType: String, val passing: Array[(String, String)]) extends StaticTestSpec with ReadMappingBehaviour {

  val testCases: Array[(Path, String)] = for {
    (folder, postProcessor) <- passing
    testCase <- StreamDataSourceTestUtil.getTestCaseFolders(folder).sorted
  } yield (testCase, postProcessor)

  "A streamer mapping reader" should behave like validMappingFile("stream/tcp")
  it should behave like invalidMappingFile("negative_test_cases")

  testCases foreach {
    case (folderPath, postProcName) =>
      it should s"produce triples equal to the expected triples for ${folderPath.getFileName}" in {
        implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
        Logger.logInfo("Starting server")
        val server: TestServer = streamType match {
          case "TCP" => TCPTestServerFactory.createServer(streamType)
          case _ => KafkaTestServerFactory.createServer(streamType)
        }
        server.setup()
        implicit val cluster: Future[MiniCluster] = StreamTestUtil.getClusterFuture(streamType)
        implicit val postProcessor: PostProcessor = TestUtil.pickPostProcessor(postProcName)
        val folder = MappingTestUtil.getFile(folderPath.toString)
        val executedFuture = StreamingTestMain.executeTestCase(folder, server)

        val result = executedFuture andThen {
          case _ =>
            server.tearDown()
            cluster.map( c => c.close())
            TestUtil.tmpCleanup(streamType)
            Logger.logError("DELETE TMP DIR")
        } andThen {
          case Success(_) =>
            Logger.logSuccess(s"Test passed!!")
            Logger.lineBreak(50)

            succeed
        } andThen {
          case Failure(exception) =>
            Logger.logError(exception.toString)
            Logger.lineBreak(50)

            fail
        }

        Await.result(result, Duration.Inf)
        Logger.logError("RESULT DONE")
      }
  }

}
