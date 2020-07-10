package io.rml.framework.api

import io.rml.framework.core.function.FunctionLoader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.util.Util

import scala.collection.mutable
import scala.reflect.io.Path

object FnOEnvironment extends Logging{
  private val functionDescriptionFilePaths : mutable.MutableList[Path] = mutable.MutableList()
  private val functionMappingFilePaths : mutable.MutableList[Path] = mutable.MutableList()
  private var functionLoader : Option[FunctionLoader] = None
  val loadedClassesMap : scala.collection.mutable.Map[String, Class[_]] = scala.collection.mutable.Map()

  def getFunctionDescriptionFilePaths() = {
    this.functionDescriptionFilePaths.toList
  }
  def addFunctionDescriptionFilePath(path : Path) : Unit = {
    this.functionDescriptionFilePaths += path
  }

  def getFunctionMappingFilePaths() = {
    this.functionMappingFilePaths.toList
  }
  def addFunctionMappingFilePaths(path : Path): Unit = {
    this.functionMappingFilePaths += path
  }

  def intializeFunctionLoader() = {
    this.functionLoader = Some(FunctionLoader.apply(getFunctionDescriptionFilePaths(), getFunctionMappingFilePaths()))
    this.functionLoader
  }
  def getFunctionLoader: Option[FunctionLoader] = {
    this.functionLoader
  }

  /**
   * Default FnO Configuration.
   * Function descriptions:
   *  - functions_grel.ttl
   *  - functions_idlab.ttl
   * Function mappings
   * - grel_java_mapping.ttl
   * - idlab_java_mapping.ttl
   *
   * RMLStreamer will look for these files in directory where the RMLStreamer is executed from.
   * Note: make sure to add jars with custom functions to Flink's `/lib` directory.
   */
  def loadDefaultConfiguration() = {

    val defaultFunctionDescriptionFilePaths = List(
      "./functions_grel.ttl",
      "./functions_idlab.ttl"
    )

    val defaultFunctionMappingFilePaths = List(
      "./grel_java_mapping.ttl",
      "./idlab_java_mapping.ttl"
    )

    // adding default function description file paths to the RMLEnvironment
    defaultFunctionDescriptionFilePaths.foreach(strPath=> {
      try
        {
          val p = Path.string2path(Util.getFile(strPath).getAbsolutePath)
          FnOEnvironment.addFunctionDescriptionFilePath(p)
        }
      catch {
        case e : Exception => logWarning(s"Can't add function description file to RMLEnvironment ( $strPath ). This will result in errors when using functions! Exception: ${e.getMessage}")
      }
    })

    // adding default function description file paths to the RMLEnvironment
    defaultFunctionMappingFilePaths.foreach(strPath=> {
      try {
        val p = Path.string2path(Util.getFile(strPath).getAbsolutePath)
        FnOEnvironment.addFunctionMappingFilePaths(p)
      }catch {
        case e : Exception => logWarning(s"Can't add function mapping file to RMLEnvironment ( $strPath ). This will result in errors when using functions! Exception: ${e.getMessage}")
      }
    })
  }
}
