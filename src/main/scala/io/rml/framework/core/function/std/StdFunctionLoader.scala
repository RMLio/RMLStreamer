package io.rml.framework.core.function.std

import io.rml.framework.core.function.model.{Parameter, FunctionMetaData}
import io.rml.framework.core.function.{FunctionLoader, FunctionUtils}
import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.jena.JenaResource
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode, RDFResource}
import io.rml.framework.core.util.{JenaUtil, Util}
import io.rml.framework.core.vocabulary.RMLVoc

case class StdFunctionLoader() extends FunctionLoader {


  override def parseFunctions(graph: RDFGraph): FunctionLoader = {
    val functionMaps = graph.filterProperties(Uri(RMLVoc.Property.LIB_PROVIDED_BY))
    logDebug("found %d transformation maps".format( functionMaps.length))

    for (map <- functionMaps) {

      val providedByTermMap = map.listProperties(RMLVoc.Property.LIB_PROVIDED_BY).head.asInstanceOf[RDFResource]

      val libPath = providedByTermMap.listProperties(RMLVoc.Property.LIB_LOCAL_LIBRARY).flatMap(Util.getLiteral).headOption
      val classNames = providedByTermMap.listProperties(RMLVoc.Property.LIB_CLASS).flatMap(Util.getLiteral)
      val methodNames = providedByTermMap.listProperties(RMLVoc.Property.LIB_METHOD).flatMap(Util.getLiteral)

      logDebug("\t" + "lib path: %s".format(libPath))
      if (libPath.nonEmpty && classNames.nonEmpty && methodNames.nonEmpty) {

        val classNameLit = classNames.head
        val methodNameLit = methodNames.head
        classLibraryMap.put(classNameLit.toString.trim, libPath.get.toString.trim)
        logDebug("\t\t" + "class: %s - method: %s".format(classNameLit, methodNameLit))
        val inputParams = parseParameterList(map, RMLVoc.Property.FNO_EXPECTS).sorted


        val outputParams = parseParameterList(map, RMLVoc.Property.FNO_RETURNS).sorted


        val functionMetaData = FunctionMetaData(libPath.get.toString.trim, classNameLit.toString.trim,
          methodNameLit.toString.trim,
          inputParams,
          outputParams
        )

        functionMap.put(map.uri, functionMetaData)
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
    val typeClass = FunctionUtils.getTypeClass(Uri(paramType))
    Parameter(typeClass, Uri(paramUri), pos)
  }
}

