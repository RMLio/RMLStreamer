package io.rml.framework.core.function

import java.io.File

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.{DynamicMethodFunction, Function, FunctionMetaData, Parameter}
import io.rml.framework.core.function.std.StdFunctionLoader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode}
import io.rml.framework.core.util.Turtle
import io.rml.framework.shared.RMLException

import scala.collection.immutable.{Map => ImmutableMap}
import scala.collection.mutable.{Map => MutableMap}

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


  def parseFunctions(file: File): FunctionLoader = {
    val graph = RDFGraph.fromFile(file, RMLEnvironment.getGeneratorBaseIRI(),Turtle)
    parseFunctions(graph)
    this
  }

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
   * Parse transformations from the [[RDFGraph]] of the whole function mapping file.
   *
   * @param graph [[RDFGraph]] representing the whole function mapping file
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

  def apply(): FunctionLoader = {

    if(singletonFunctionLoader.isEmpty) {
      // construct functionDescriptionTriplesGraph
      val functionsGrelFile = new File(getClass.getClassLoader.getResource("functions_grel.ttl").getFile)
      if (!functionsGrelFile.exists())
        throw new RMLException(s"Couldn't find ${functionsGrelFile.getName}")

      // construct function description graph
      // this graph will be used by the function loader to map the function descriptions to their implementations
      val functionDescriptionTriplesGraph = RDFGraph.fromFile(functionsGrelFile, RMLEnvironment.getGeneratorBaseIRI(), Turtle)
      // construct functionLoader
      singletonFunctionLoader = Some(StdFunctionLoader(functionDescriptionTriplesGraph))
    }
    singletonFunctionLoader.get
  }

}


