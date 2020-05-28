package io.rml.framework.core.function

import java.io.File

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.model.{DynamicMethodTransformation, Parameter, Transformation, TransformationMetaData}
import io.rml.framework.core.function.std.StdTransformationLoader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode}
import io.rml.framework.core.util.Turtle

import scala.collection.immutable.{Map => ImmutableMap}
import scala.collection.mutable.{Map => MutableMap}

abstract class TransformationLoader extends Logging {
  /**
   * Map string value of classes to library path
   */
  protected val classLibraryMap: MutableMap[String, String] = MutableMap()


  /**
   * Map names of [[Transformation]] to concrete [[Transformation]] object
   */
  protected val transformationMap: MutableMap[Uri, TransformationMetaData] = MutableMap()


  def getClassLibraryMap: ImmutableMap[String, String] = classLibraryMap.toMap

  def getTransformationMap = transformationMap.toMap


  def parseTransformations(file: File): TransformationLoader = {

    //TODO: passing hardcoding Turtle-format.
    // HELP: shouldn't we derive the format from the file itself?
    val graph = RDFGraph.fromFile(file, RMLEnvironment.getGeneratorBaseIRI(),Turtle)

    parseTransformations(graph)
    this
  }

  /**
   * Given the [[Uri]] representation of the transformation, the [[TransformationLoader]]
   * will search for the transformation in the [[transformationMap]] and dynamically load
   * the transformation.
   *
   * @param uri Uri representing a transformation
   * @return  [[Option]] of dynamically loaded transformation
   */
  def loadTransformation(uri: Uri): Option[Transformation] = {
    logInfo(s"loadTransformation: ${uri.uri}")

    val optTransformation = transformationMap.get(uri)

    if (optTransformation.isDefined) {
      val trans = optTransformation.get

      logInfo(s"Dynamically loading transformation: $uri, ${trans.toString}" )

      trans match {
        case transformationMetaData: TransformationMetaData => {
          //          val loadedTrans = transient.initialize()
          //          transformationMap.put(uri, loadedTrans)
          //          Some(loadedTrans)

          Some(Transformation(transformationMetaData.identifier, transformationMetaData))
          }

        case loadedTrans: DynamicMethodTransformation => Some(loadedTrans)
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
   * @return [[TransformationLoader]]
   */
  def parseTransformations(graph: RDFGraph): TransformationLoader


  /**
   * Parse [[Parameter]] from the given [[RDFNode]] which represents the parameter
   *
   * @param rdfNode [[RDFNode]] containing the mapping information of the parameter
   * @param pos [[Int]] integer position of the parameter
   * @return
   */
  def parseParameter(rdfNode: RDFNode, pos: Int): Parameter
}


object TransformationLoader {

  def apply(): TransformationLoader = StdTransformationLoader()
}


