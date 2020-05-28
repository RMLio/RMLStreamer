package io.rml.framework.core.model.std

import io.rml.framework.core.function.TransformationLoader
import io.rml.framework.core.model.TransformationMapping

case class StdTransformationMapping(identifier: String, transformationLoader: TransformationLoader) extends TransformationMapping {
}
