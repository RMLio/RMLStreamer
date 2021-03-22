package io.rml.framework.core.function

import io.rml.framework.api.{FnOEnvironment, RMLEnvironment}
import io.rml.framework.core.function.model.{Function, FunctionMetaData, Parameter}
import io.rml.framework.core.function.std.StdFunctionLoader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode}
import io.rml.framework.core.util.Turtle

import java.io.{File, IOException}
import scala.collection.mutable.{Map => MutableMap}
import scala.reflect.io.Path

abstract class FunctionLoader extends Logging {
  /**
   * Map Function Uri to FunctionMetaData object
   */
  protected val functionMap: MutableMap[Uri, FunctionMetaData] = MutableMap()

  def getSources: List[String] = {
    functionMap.values.map(_.source).toList.distinct
  }
  def getClassNames : List[String] = {
    functionMap.values.map(_.className).toList.distinct
  }
  /**
   * Creates and returns a [[Function]] for [[FunctionMetaData]] given function [[Uri]], if the function uri is present.
   *
   * @param uri: function uri
   * @return  function (if successful)
   */
  def createFunction(uri: Uri): Option[Function] = {
    logDebug(s"createFunction: ${uri.value}")

    val optFunctionMetaData = functionMap.get(uri)

    if (optFunctionMetaData.isDefined) {
      val functionMetaData = optFunctionMetaData.get
      logDebug(s"Creating function: $uri, ${functionMetaData.toString}" )
      Some(Function(functionMetaData.identifier, functionMetaData))
    } else {
      // when the function uri is not present in the function map, complain.
      val availableFunctionURIs = functionMap.keys.map(u=>u.toString)
      throw new IOException(s"The function with URI ${uri.value} can not be found.\n" +
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

  /**
   * Private helper method for reading in the function descriptions as an RDFGraph.
   * @param filePath
   * @return RDFGraph containing the function descriptions.
   */
  private def readFunctionDescriptionsFromFile(filePath : String): RDFGraph = {
    logDebug(s"FunctionLoader is reading function descriptions from : ${filePath}")
    RDFGraph.fromFile(new File(filePath), RMLEnvironment.getGeneratorBaseIRI(), Turtle)
  }

  /**
   * Construction of the (singleton) FunctionLoader instance.
   * @return FunctionLoader
   */
  def apply(functionDescriptionPaths : List[Path] = FnOEnvironment.getFunctionDescriptionFilePaths(),
            functionMappingPaths : List[Path] = FnOEnvironment.getFunctionMappingFilePaths()
           ): Option[FunctionLoader] = {
      if(singletonFunctionLoader.isEmpty) {

        // The functionDescriptionsGraph is populated by iterating over the filepaths of the function description files.
        // These function description files are obtained from the RMLEnvironment.
        val fdit =functionDescriptionPaths.map(_.path).iterator
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
        if(functionDescriptionsGraph.isDefined) {
          singletonFunctionLoader = Some(StdFunctionLoader(functionDescriptionsGraph.get))
          // now parse the mappings
          functionMappingPaths.foreach(fmp => {
            singletonFunctionLoader.get.parseFunctionMapping(new File(fmp.path))
          })

        } else {
          logWarning("No function graph found. Continuing without loading functions.")
          None
        }
      }
      singletonFunctionLoader
    }
}


