package io.rml.framework.core.function

import java.io.File

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.{DynamicMethodFunction, Function, FunctionMetaData, Parameter}
import io.rml.framework.core.function.std.StdFunctionLoader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode}
import io.rml.framework.core.util.Turtle
import io.rml.framework.shared.{FnOException, RMLException}

import scala.collection.immutable.{Map => ImmutableMap}
import scala.collection.mutable
import scala.collection.mutable.{MutableList, Map => MutableMap}

abstract class FunctionLoader extends Logging {
  /**
   * Map string value of classes to library path
   */
  protected val classLibraryMap: MutableMap[String, String] = MutableMap()


  /**
   * Map names of [[Function]] to concrete [[Function]] object
   */
  protected val functionMap: MutableMap[Uri, FunctionMetaData] = MutableMap()

  def getClassLibraryMap: ImmutableMap[String, String] = classLibraryMap.toMap

  def getFunctionMap = functionMap.toMap


  /**
   * Given the [[Uri]] representation of the transformation, the [[FunctionLoader]]
   * will search for the transformation in the [[functionMap]] and dynamically load
   * the transformation.
   *
   * @param uri Uri representing a transformation
   * @return  [[Option]] of dynamically loaded transformation
   */
  def loadFunction(uri: Uri): Option[Function] = {
    logDebug(s"loadFunction: ${uri.uri}")

    val optTransformation = functionMap.get(uri)

    if (optTransformation.isDefined) {
      val trans = optTransformation.get

      logDebug(s"Dynamically loading function: $uri, ${trans.toString}" )

      trans match {
        case transformationMetaData: FunctionMetaData => {
          //          val loadedTrans = transient.initialize()
          //          functionMap.put(uri, loadedTrans)
          //          Some(loadedTrans)

          Some(Function(transformationMetaData.identifier, transformationMetaData))
          }

        case loadedFunction: DynamicMethodFunction => Some(loadedFunction)
        case _ => None
      }

    } else {
      None
    }
  }

  /**
   * The given `functionMappingFile` should be a Turtle-file containing the function mappings. These mappings will be parsed and
   * the FunctionLoader's functionMap is updated accordingly.
   *
   * @param functionMappingFile
   * @return
   */
  def parseFunctions(functionMappingFile: File): FunctionLoader = {
    val graph = RDFGraph.fromFile(functionMappingFile, RMLEnvironment.getGeneratorBaseIRI(),Turtle)
    parseFunctions(graph)
    this
  }


  /**
   * The given `graph` should contain the function mappings. These mappings will be parsed and
   * the FunctionLoader's functionMap is updated accordingly.
   *
   * @param graph [[RDFGraph]] representing a function mapping
   * @return [[FunctionLoader]]
   */
  def parseFunctions(graph: RDFGraph): FunctionLoader




  /**
   * Parse [[Parameter]] from the given [[RDFNode]] which represents the parameter
   *
   * @param rdfNode [[RDFNode]] containing the mapping information of the parameter
   * @param pos [[Int]] integer position of the parameter
   * @return
   */
  def parseParameter(rdfNode: RDFNode, pos: Int): Parameter


}


object FunctionLoader {

  private var singletonFunctionLoader : Option[FunctionLoader] = None

  private val defaultFunctionDescriptionFilePaths = List(
    "functions_grel.ttl"
  )

  private def readFunctionDescriptionsFromFile(filePath : String): RDFGraph = {
    val functionDescriptionsFile = new File(getClass.getClassLoader.getResource(filePath).getFile)
    if (!functionDescriptionsFile.exists())
      throw new RMLException(s"Couldn't find ${functionDescriptionsFile.getName}")

    RDFGraph.fromFile(functionDescriptionsFile, RMLEnvironment.getGeneratorBaseIRI(), Turtle)
  }

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
        throw new FnOException("No function description functionMappingGraph was created...")
    }
    singletonFunctionLoader.get
  }

}


