package io.rml.framework

import java.io.File

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.FunctionLoader
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.reflect.io.Path


object FunctionMappingSetup {
  def setupFunctionLoader() = {
    // function descriptions
    val functionDescriptionFilePaths = List(
      "functions_grel.ttl",
      "functions_idlab.ttl"
    ).map(fp=>
    {
      val f = new File(getClass.getClassLoader.getResource(fp).getFile)
      Path.apply(f)
    }
    )

    // add them to the RMLEnvironment
    functionDescriptionFilePaths.foreach(p=>RMLEnvironment.addFunctionDescriptionFilePath(p))


    // function mappings
    val functionMappingPaths = List(
      "grel_java_mapping.ttl",
      "idlab_java_mapping.ttl"
    ).map(fp=>
    {
      val f = new File(getClass.getClassLoader.getResource(fp).getFile)
      Path.apply(f)
    }
    )

    // add them to the RMLEnvironment
    functionMappingPaths.foreach(p=>RMLEnvironment.addFunctionMappingFilePaths(p))

    // singleton FunctionLoader created and initialized with function descriptions and mappings from the RMLENvironment
    val functionLoader = FunctionLoader()
  }
}
trait FunctionMappingTest extends FunSuite with BeforeAndAfter {


  before {
    FunctionMappingSetup.setupFunctionLoader()
  }
}