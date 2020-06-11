package io.rml.framework.core.function

import java.io.File

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.{DynamicMethodFunction, Parameter, Function, FunctionMetaData}
import io.rml.framework.core.function.std.StdFunctionLoader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode}
import io.rml.framework.core.util.Turtle

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

    //TODO: passing hardcoding Turtle-format.
    // HELP: shouldn't we derive the format from the file itself?
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

  def apply(): FunctionLoader = StdFunctionLoader()
}


