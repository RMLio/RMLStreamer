package io.rml.framework

import io.rml.framework.core.internal.Logging
import io.rml.framework.engine.NopPostProcessor
import io.rml.framework.flink.util.FunctionsFlinkUtil
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
 * Test helper trait that automatically initializes the function loader with the default configuration.
 */
trait FunctionMappingTest extends FunSuite  with BeforeAndAfterAll with Logging {
  implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  implicit val senv :StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  implicit val postProcessor = new NopPostProcessor()

  override protected def beforeAll(): Unit = {
    logInfo("Before All")
    FunctionsFlinkUtil.putFunctionFilesInFlinkCache(env.getJavaEnv, senv.getJavaEnv,
      "functions_grel.ttl",
     "grel_java_mapping.ttl",
     "fno/functions_idlab.ttl",
     "fno/functions_idlab_test_classes_java_mapping.ttl"
    )
  }
}