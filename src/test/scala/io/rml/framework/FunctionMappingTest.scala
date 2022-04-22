package io.rml.framework

import io.rml.framework.api.FnOEnvironment
import io.rml.framework.core.internal.Logging
import org.scalatest.{BeforeAndAfterAll, FunSuite}

object FunctionMappingSetup {
  def setupFunctionAgent() = {
    // singleton function Agent created and initialized with default function descriptions and function mappings
    FnOEnvironment()
  }
}

/**
 * Test helper trait that automatically initializes the function loader with the default configuration.
 */
trait FunctionMappingTest extends FunSuite  with BeforeAndAfterAll with Logging{

  override protected def beforeAll(): Unit = {
    logInfo("Before All")
    FunctionMappingSetup.setupFunctionAgent()
  }
}