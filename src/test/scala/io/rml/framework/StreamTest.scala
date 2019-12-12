package io.rml.framework


/**
  * <p>Copyright 2019 IDLab (Ghent University - imec)</p>
  *
  * @author Gerald Haesendonck
  */
abstract class StreamTest(val streamType: String, val passing: Array[(String, String)]) extends StaticTestSpec with ReadMappingBehaviour {

  /*val testCases: Array[(Path, String)] = for {
    (folder, postProcessor) <- passing
    testCase <- StreamDataSourceTestUtil.getTestCaseFolders(folder).sorted
  } yield (testCase, postProcessor)

  "A streamer mapping reader" should behave like validMappingFile("stream/tcp")
  it should behave like invalidMappingFile("negative_test_cases")

  val random = scala.util.Random
  testCases foreach {
    case (folderPath, postProcName) =>
      val test = streamType + '_' + random.nextLong
      it should s"produce triples equal to the expected triples for ${folderPath.getFileName}" in {
        implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
        implicit val senv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
        Logger.logInfo("Starting server")
        val server: TestServer = streamType match {
          case "TCP" => TCPTestServerFactory.createServer(test)
          case _ => KafkaTestServerFactory.createServer(test)
        }
        server.setup()
        implicit val cluster: Future[MiniCluster] = StreamTestUtil.getClusterFuture(test)
        implicit val postProcessor: PostProcessor = TestUtil.pickPostProcessor(postProcName)
        val folder = MappingTestUtil.getFile(folderPath.toString)
        val executedFuture = StreamingTestMain.executeTestCase(folder, server)

        val result = executedFuture andThen {
          case _ =>
            cluster.map(c => {
              Logger.logInfo("Stopping Flink cluster...")
              c.close()
              Logger.logInfo("Flink cluster stopped!")
            }).wait()
            server.tearDown()
            TestUtil.tmpCleanup(test)

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
      }
  }*/

}
