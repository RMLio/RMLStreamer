package io.rml.framework.core.function

import java.io.{File, FileNotFoundException, IOException}

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.{Function, FunctionMetaData, Parameter}
import io.rml.framework.core.function.std.StdFunctionLoader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode}
import io.rml.framework.core.util.Turtle

import scala.collection.mutable.{Map => MutableMap}

abstract class FunctionLoader extends Logging {
  /**
   * Map Function Uri to FunctionMetaData object
   */
  protected val functionMap: MutableMap[Uri, FunctionMetaData] = MutableMap()

  /**
   * Searches for the given function uri in the function map and dynamically load
   * that function, if present.
   *
   * @param uri Function Uri
   * @return  [[Option]] of dynamically loaded function
   */
  def loadFunction(uri: Uri): Option[Function] = {
    logDebug(s"loadFunction: ${uri.uri}")

    val optFunctionMetaData = functionMap.get(uri)

    if (optFunctionMetaData.isDefined) {
      val functionMetaData = optFunctionMetaData.get
      logDebug(s"Dynamically loading function: $uri, ${functionMetaData.toString}" )
      Some(Function(functionMetaData.identifier, functionMetaData))
    } else {
      // when the function uri is not present in the function map, complain.
      val availableFunctionURIs = functionMap.keys.map(u=>u.toString)
      throw new IOException(s"The function with URI ${uri.toString} can not be found.\n" +
        s"The available function URIs are: " + availableFunctionURIs)
    }
  }

  /**
   * The given `functionMappingFile` should be a Turtle-file containing the function mappings. These mappings will be parsed and
   * the FunctionLoader's functionMap is updated accordingly.
   *
   * @param functionMappingFile
   * @return
   */
  def parseFunctionMapping(functionMappingFile: File): FunctionLoader = {
    val graph = RDFGraph.fromFile(functionMappingFile, RMLEnvironment.getGeneratorBaseIRI(),Turtle)
    parseFunctionMapping(graph)
    this
  }

  /**
   * The given `graph` should contain the function mappings. These mappings will be parsed and
   * the FunctionLoader's functionMap is updated accordingly.
   *
   * @param graph [[RDFGraph]] representing a function mapping
   * @return [[FunctionLoader]]
   */
  def parseFunctionMapping(graph: RDFGraph): FunctionLoader

  /**
   * Parse [[Parameter]] from the given [[RDFNode]] which represents the parameter
   *
   * @param rdfNode [[RDFNode]] containing the mapping information of the parameter
   * @param pos [[Int]] integer position of the parameter
   * @return
   */
  def parseParameter(rdfNode: RDFNode, pos: Int): Parameter

}


object FunctionLoader extends Logging{
  private var singletonFunctionLoader : Option[FunctionLoader] = None

  private val defaultFunctionDescriptionFilePaths = List(
    "functions_grel.ttl",
    "functions_idlab.ttl"
  )

  /**
   * Private helper method for reading in the function descriptions as an RDFGraph.
   * @param filePath
   * @return RDFGraph containing the function descriptions.
   */
  private def readFunctionDescriptionsFromFile(filePath : String): RDFGraph = {
    val functionDescriptionsFile = new File(getClass.getClassLoader.getResource(filePath).getFile)
    if (!functionDescriptionsFile.exists())
      throw new FileNotFoundException(s"Couldn't find ${functionDescriptionsFile.getName}")

    logDebug(s"FunctionLoader is reading function descriptions from : ${filePath}")
    RDFGraph.fromFile(functionDescriptionsFile, RMLEnvironment.getGeneratorBaseIRI(), Turtle)
  }

  /**
   * Construction of the (singleton) FunctionLoader instance.
   * When the functionDescriptionFilePaths-list is empty, the default function descriptions are used.
   * @param functionDescriptionFilePaths filepaths to the function descriptions. Default value is an empty list.
   * @return FunctionLoader
   */
  def apply(functionDescriptionFilePaths : List[String] = List()): FunctionLoader = {

    if(singletonFunctionLoader.isEmpty) {

      // The functionDescriptionsGraph is populated by iterating over the filepaths of the function description files.
      // When no filepaths are provided (i.e. functionDescriptionFilePaths is empty), the function loader will use the
      // default function description files (i.e. defaultFunctionDescriptionFilePaths)
      val fdit = if(functionDescriptionFilePaths.isEmpty) defaultFunctionDescriptionFilePaths.iterator else functionDescriptionFilePaths.iterator

      // construct the initial functionDescriptionTriplesGraph using the first functiondescription filepath
      val functionDescriptionsGraph : Option[RDFGraph] =
        if(fdit.hasNext)
          Some(readFunctionDescriptionsFromFile(fdit.next()))
      else
        None

      // If more function description filepaths are specified, they will be read in.
      // The resulting triples will be added to the functionDescriptionsGraph
      while (fdit.hasNext) {
        val fdescGraph = readFunctionDescriptionsFromFile(fdit.next())
        functionDescriptionsGraph.get.addTriples(fdescGraph.listTriples)
      }

      // construct functionLoader
      if(functionDescriptionsGraph.isDefined)
        singletonFunctionLoader = Some(StdFunctionLoader(functionDescriptionsGraph.get))
      else
        throw new Exception("Function description graph is none...")
    }
    singletonFunctionLoader.get
  }

}


