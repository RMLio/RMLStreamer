package io.rml.framework.core.model.std

import io.rml.framework.core.function.FunctionLoader
import io.rml.framework.core.model.FunctionMapping

case class StdFunctionMapping(identifier: String, functionLoader: FunctionLoader) extends FunctionMapping {
}
