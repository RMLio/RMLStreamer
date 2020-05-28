package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.MappingExtractor
import io.rml.framework.core.function.TransformationLoader
import io.rml.framework.core.model.TransformationMapping
import io.rml.framework.core.model.rdf.RDFGraph

class StdTransformationMappingExtractor extends MappingExtractor[TransformationMapping] {
  /**
   * Extract.
   *
   * @param node Node to extract from.
   * @return
   */
  override def extract(node: RDFGraph): TransformationMapping =  {
    val loader = TransformationLoader()

    loader.parseTransformations(node)

    TransformationMapping("defaulty", loader)
  }
}
