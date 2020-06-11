package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.{FunctionMapExtractor, LogicalSourceExtractor, PredicateObjectMapExtractor, SubjectMapExtractor}
import io.rml.framework.core.model.FunctionMap
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.vocabulary.RMLVoc

class StdFunctionMapExtractor(subjectMapExtractor: SubjectMapExtractor,
                              logicalSourceExtractor: LogicalSourceExtractor) extends FunctionMapExtractor {

  /**
   * Lazy val to prevent circular reference and stack overflow when instantiating the class
   */
  lazy val pomExtractor = PredicateObjectMapExtractor()


  /**
   * Extract.
   *
   * @param node Node to extract from.
   * @return
   */
  override def extract(node: RDFResource): List[FunctionMap] = {
    this.logDebug("%s extract(node)".format(this.getClass.getName))
    extractFunctionMap("", node)
  }


  /**
   * Extracts function term map in the mapping file and
   * load the function specified into the static RMLEnvironment class
   *
   * @param resource
   * @return
   */
  private def extractFunctionMap(fnParentMap: String, resource: RDFResource): List[FunctionMap] = {
    val functionValues = resource.listProperties(RMLVoc.Property.FUNCTIONVALUE)

    require(functionValues.size <= 1, "At most only 1 function value allowed.")
    val result = functionValues.map(node => {
      val functionValue = node.asInstanceOf[RDFResource]

      val poms = pomExtractor.extract(functionValue)

      FunctionMap(fnParentMap, functionValue.uri.toString, poms)

    })

    result
  }

  override def loadFunctionsIntoEnvironment(): Unit = {
    throw new NotImplementedError()
  }
}
