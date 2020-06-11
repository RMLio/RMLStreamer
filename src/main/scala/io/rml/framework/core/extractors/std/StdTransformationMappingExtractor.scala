package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.MappingExtractor
import io.rml.framework.core.function.FunctionLoader
import io.rml.framework.core.model.FunctionMapping
import io.rml.framework.core.model.rdf.RDFGraph

//TODO: to be consistent with the terminology, Function should be replaced with Function
class StdTransformationMappingExtractor extends MappingExtractor[FunctionMapping] {
  /**
   * Extract.
   *
   * @param node Node to extract from.
   * @return
   */
  override def extract(node: RDFGraph): FunctionMapping =  {
    val loader = FunctionLoader()

    loader.parseFunctions(node)

    FunctionMapping("defaulty", loader)
  }
}
