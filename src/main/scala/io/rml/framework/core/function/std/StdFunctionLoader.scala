package io.rml.framework.core.function.std

import java.net.MalformedURLException

import io.rml.framework.core.extractors.SubjectMapExtractor
import io.rml.framework.core.function.FunctionUtils.logError
import io.rml.framework.core.function.{FunctionLoader, FunctionUtils}
import io.rml.framework.core.function.model.{FunctionMetaData, Parameter}
import io.rml.framework.core.model.{Uri, rdf}
import io.rml.framework.core.model.rdf.{RDFGraph, RDFNode, RDFResource, RDFTriple}
import io.rml.framework.core.model.rdf.jena.JenaResource
import io.rml.framework.core.util.{JenaUtil, Util}
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.shared.{FnOException, RMLException}


case class StdFunctionLoader(functionDescriptionTriplesGraph : RDFGraph) extends FunctionLoader {

  override def parseFunctionMapping(graph: RDFGraph): FunctionLoader = {
    logDebug("parsing functions the new way (i.e. using StdFunctionLoader)")

    // a fnoi:Mapping
    //  property: fno:function
    //  property: fno:implementation
    //            an fno:implementation resource have type a like
    //                - fnoi:JavaClass
    //
    val fnoFunctionProperty = Uri(RMLVoc.Property.FNO_FUNCTION)

    // subject resources with fno:function property
    // these resources have type fnoi:Mapping
    val mappings = graph.filterProperties(fnoFunctionProperty)
    if(mappings.isEmpty)
      throw new RMLException("No function mappings found...")

    val functionDescriptionResources = this.functionDescriptionTriplesGraph.filterResources(Uri(RMLVoc.Class.FNO_FUNCTION))
    logDebug(s"${functionDescriptionResources.length} functionDescriptionResources present")
    logDebug(s"The current function description graph contains ${mappings.length} mappings")
    for (map <- mappings) {
      logDebug(s"Processing mapping: ${map.uri}")
      try {
        val functionUri = map.listProperties(RMLVoc.Property.FNO_FUNCTION).head.asInstanceOf[RDFResource].uri

        val methodMappingResource = map.listProperties(RMLVoc.Property.FNO_METHOD_MAPPING).head.asInstanceOf[RDFResource]
        val methodName = methodMappingResource.listProperties(RMLVoc.Property.FNOM_METHOD_NAME).head.toString
        val implementationResource = map.listProperties(RMLVoc.Property.FNO_IMPLEMENTATION).head.asInstanceOf[RDFResource]

        val className = Util.getLiteral(implementationResource.listProperties(RMLVoc.Property.FNOI_CLASS_NAME).head)
        val downloadPage = Util.getLiteral(implementationResource.listProperties(RMLVoc.Property.DOAP_DOWNLOAD_PAGE).head)
        logDebug(s"Found map with methodname: ${methodName}, className: ${className}, downloadPage: ${downloadPage}")

        // Get function description resource that corresponds with the current functionUri
        // If not present, throw appropriate exception
        val functionDescriptionResourceOption = functionDescriptionResources.find(fd => fd.uri == functionUri)
        if(functionDescriptionResourceOption.isEmpty)
          throw new FnOException(s"No function description resource found with uri: ${functionUri}")


        // extraction of input parameters
        val expectsResource = functionDescriptionResourceOption.get.listProperties(RMLVoc.Property.FNO_EXPECTS).headOption
        val inputParameterResources = expectsResource.get.asInstanceOf[RDFResource].getList.asInstanceOf[List[RDFResource]]
        val inputParamList = parseParameterResources(inputParameterResources)

        // extraction of output parameters
        val returnsResource = functionDescriptionResourceOption.get.listProperties(RMLVoc.Property.FNO_RETURNS).headOption
        val outputParameterResources = returnsResource.get.asInstanceOf[RDFResource].getList.asInstanceOf[List[RDFResource]]
        val outputParamList = parseParameterResources(outputParameterResources)

        // construct function meta data object and store it in the functionMap
        val functionMetaData = FunctionMetaData(downloadPage.get.value, className.get.value, methodName, inputParamList, outputParamList)
        this.functionMap.put(functionUri, functionMetaData)

      }catch {
        case e@(_: RMLException | _: FnOException) =>
          logError(e.getMessage)
      }

    }

    logDebug(s"${this.functionMap.size} functions are parsed. The function maps contains the following functions")
    this.functionMap.foreach{
      kv =>
        logDebug(s"\t${kv._1}")
    }
    this
  }


  def parseParameterResources(parameterResources : List[RDFResource]) : List[Parameter] = {
    parameterResources.zipWithIndex.map{
      case (paramResource, paramIndex) =>
        parseParameter(paramResource, paramIndex)
    }
  }

  override def parseParameter(inputNode: RDFNode, pos: Int): Parameter = {
    val inputResource = inputNode.asInstanceOf[JenaResource]
    val paramType = inputResource.listProperties(RMLVoc.Property.FNO_TYPE).headOption
    val paramUri = inputResource.listProperties(RMLVoc.Property.FNO_PREDICATE).headOption


    if(paramType.isEmpty)
      throw new FnOException(s"Parameter Type not defined for parameter resource: ${inputResource.uri}")

    if(paramUri.isEmpty)
      throw new FnOException(s"Parameter Uri not defined for parameter resource: ${inputResource.uri}")


    val typeClass = FunctionUtils.getTypeClass(Uri(paramType.get.toString))
    Parameter(typeClass, Uri(paramUri.get.toString), pos)
  }
}
