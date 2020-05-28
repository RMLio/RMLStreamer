package io.rml.framework.core.model

import io.rml.framework.core.function.TransformationLoader
import io.rml.framework.core.model.std.StdTransformationMapping

trait TransformationMapping extends Graph {

  /**
   * A transformation loader object which contains information
   * in the mapping document to load function dynamically.
   *
   * Note: Functions are not loaded from the libraries
   * at the moment of initialization of the [[TransformationLoader]].
   * It only has information on library path and classes containing
   * the function.
   * Functions will be loaded on demand when reading through the
   * RML mapping file.
   *
   * @return
   */
  def transformationLoader: TransformationLoader


}

object TransformationMapping {

  //TODO: Temporary solution to passing the mapping object to function map extractors!
  //  Need refactoring!
  private var mapping: Option[TransformationMapping] = None


  def getOpt: Option[TransformationMapping] = {
    mapping
  }

  def apply(identifier: String, transformationLoader: TransformationLoader): TransformationMapping = {
      val defMapping = StdTransformationMapping(identifier, transformationLoader)
      mapping = Some(defMapping)
      defMapping
    
  }
}
