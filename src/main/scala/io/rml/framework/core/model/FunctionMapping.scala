package io.rml.framework.core.model

import io.rml.framework.core.function.FunctionLoader
import io.rml.framework.core.model.std.StdFunctionMapping


trait FunctionMapping extends Graph {

  /**
   * A transformation loader object which contains information
   * in the mapping document to load function dynamically.
   *
   * Note: Functions are not loaded from the libraries
   * at the moment of initialization of the [[FunctionLoader]].
   * It only has information on library path and classes containing
   * the function.
   * Functions will be loaded on demand when reading through the
   * RML mapping file.
   *
   * @return
   */
  def functionLoader: FunctionLoader


}

object FunctionMapping {

  //TODO: Temporary solution to passing the mapping object to function map extractors!
  //  Need refactoring!
  private var mapping: Option[FunctionMapping] = None


  def getOpt: Option[FunctionMapping] = {
    mapping
  }

  def apply(identifier: String, functionLoader: FunctionLoader): FunctionMapping = {
      val defMapping = StdFunctionMapping(identifier, functionLoader)
      mapping = Some(defMapping)
      defMapping
    
  }
}
