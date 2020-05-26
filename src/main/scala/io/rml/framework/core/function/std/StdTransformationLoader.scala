package io.rml.framework.core.function.std

import io.rml.framework.core.function.model.{Parameter, TransientTransformation}
import io.rml.framework.core.function.{TransformationLoader, TransformationUtils}
import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.jena.JenaResource
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode, RDFResource}
import io.rml.framework.core.util.{JenaUtil, Util}
import io.rml.framework.core.vocabulary.RMLVoc

case class StdTransformationLoader() extends TransformationLoader {


  override def parseTransformations(graph: RDFGraph): TransformationLoader = {
    val transformationMaps = graph.filterProperties(Uri(RMLVoc.Property.LIB_PROVIDED_BY))


    for (map <- transformationMaps) {

      val providedByTermMap = map.listProperties(RMLVoc.Property.LIB_PROVIDED_BY).head.asInstanceOf[RDFResource]

      val libPath = providedByTermMap.listProperties(RMLVoc.Property.LIB_LOCAL_LIBRARY).flatMap(Util.getLiteral).headOption
      val classNames = providedByTermMap.listProperties(RMLVoc.Property.LIB_CLASS).flatMap(Util.getLiteral)
      val methodNames = providedByTermMap.listProperties(RMLVoc.Property.LIB_METHOD).flatMap(Util.getLiteral)

      if (libPath.nonEmpty && classNames.nonEmpty && methodNames.nonEmpty) {

        val classNameLit = classNames.head
        val methodNameLit = methodNames.head
        classLibraryMap.put(classNameLit.toString.trim, libPath.get.toString.trim)

        val inputParams = parseParameterList(map, RMLVoc.Property.FNO_EXPECTS).sorted


        val outputParams = parseParameterList(map, RMLVoc.Property.FNO_RETURNS).sorted


        val transformation = TransientTransformation(libPath.get.toString.trim, classNameLit.toString.trim,
          methodNameLit.toString.trim,
          inputParams,
          outputParams
        )

        transformationMap.put(map.uri, transformation)
      }
    }

    this
  }

  def parseParameterList(resource: RDFResource, property: String): List[Parameter] = {
    JenaUtil
      .parseListNodes(resource, property)
      .map { case (node, pos) => parseParameter(node, pos) }

  }

  override def parseParameter(inputNode: RDFNode, pos: Int): Parameter = {
    val inputResource = inputNode.asInstanceOf[JenaResource]
    val paramType = inputResource.listProperties(RMLVoc.Property.FNO_TYPE).head.toString
    val paramUri = inputResource.listProperties(RMLVoc.Property.FNO_PREDICATE).head.toString
    val typeClass = TransformationUtils.getTypeClass(Uri(paramType))
    Parameter(typeClass, Uri(paramUri), pos)
  }
}

