package io.rml.framework.util

import java.io.File
import java.util.concurrent.CompletableFuture

import io.rml.framework.Main
import io.rml.framework.engine.PostProcessor
import io.rml.framework.util.fileprocessing.MappingTestUtil
import io.rml.framework.util.logging.Logger
import org.apache.flink.api.common.JobID
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.jobgraph.JobGraph
import org.apache.flink.runtime.messages.Acknowledge
import org.apache.flink.runtime.minicluster.{MiniCluster, MiniClusterConfiguration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future}

object StreamTestUtil {

  /**
    * Create a data stream from mapping file in de specified test case folder.
    * The method won't run it yet as a job in flink cluster.
    *
    * This is used to generate and obtain job graphs which will be used by the
    * cluster to schedule/cancel jobs.
    *
    * @param testCaseFolder folder containing rml mapping file
    * @return flink DataStream[String]
    */
  def createDataStream(testCaseFolder: File)(implicit senv: StreamExecutionEnvironment,
                                             env: ExecutionEnvironment,
                                             postProcessor: PostProcessor): DataStream[String] = {

    // read the mapping
    val formattedMapping = MappingTestUtil.processFilesInTestFolder(testCaseFolder.getAbsolutePath)
    val stream = Main.createStreamFromFormattedMapping(formattedMapping.head)

    stream
  }

  /**
    * Cancels a job with the given JobID in the cluster.
    *
    * See line 550 of src/main/java/org/apache/flink/client/CliFrontend.java in
    * https://github.com/apache/flink/tree/release-1.3.2-rc3/flink-clients
    *
    * @param jobID
    * @param cluster
    * @param duration
    */

  def cancelJob(jobID: JobID, cluster: MiniCluster, duration: FiniteDuration = FiniteDuration(4, "seconds"))(implicit executur: ExecutionContextExecutor): CompletableFuture[Acknowledge] = {
    cluster.cancelJob(jobID)
    /*val actorGateFuture = cluster
    for {
      gateway <- actorGateFuture
      result <- gateway.ask(new CancelJob(jobID), duration)
    } yield result*/

  }


  /**
    * Submit a job specified in the data stream to the given mini flink cluster.
    *
    * @param cluster a flink cluster
    * @param dataStream
    * @param name    tag for the job which will be submitted to cluster
    * @tparam T result type of the data stream
    * @return JobID of the submitted job which can be used later on to cancel/stop
    */
  def submitJobToCluster[T](cluster: MiniCluster, dataStream: DataStream[T], name: String)(implicit executur: ExecutionContextExecutor): Future[JobID] =

    Future {
      while (cluster.requestClusterOverview().get().getNumJobsRunningOrPending > 1) {
        Thread.sleep(100)
      }

      val graph = dataStream.executionEnvironment.getStreamGraph
      graph.setJobName(name)
      val jobGraph: JobGraph = graph.getJobGraph
      cluster.runDetached(jobGraph)
      Logger.logInfo(cluster.requestClusterOverview().get().getNumJobsRunningOrPending().toString)

      jobGraph.getJobID

    }


  /**
    * Starts a local mini cluster to let scala tests have more control
    * over the flink jobs (cancelling, adding, multiple jobs, etc...)
    *
    * The cluster will be run on a separate thread.
    *
    *
    * See link below for usage of flink cluster.
    * https://stackoverflow.com/questions/43871438/can-flink-attach-multiple-jobs-to-stream-local-envirnoment-with-web-ui-by-java-c
    *
    * @return Future[LocalFlinkMiniCluster] containing the cluster class
    *         which can be used for stopping/cancelling and starting jobs
    */

  def getClusterFuture(test: String)(implicit executor: ExecutionContextExecutor): Future[MiniCluster] = {
    Logger.logInfo("Starting up cluster....")

    val customConfig = new Configuration()
    customConfig.setString("io.tmp.dirs", TestProperties.getTempDir(test).toString)
    val configuration = new MiniClusterConfiguration.Builder()
      .setConfiguration(customConfig)
      .setNumTaskManagers(1)
      .setNumSlotsPerTaskManager(100)
      .build()
    // start cluster
    val cluster = new MiniCluster(configuration)

    cluster.start()
    Logger.logInfo("Cluster started")
    Future.successful(cluster)
  }


}
