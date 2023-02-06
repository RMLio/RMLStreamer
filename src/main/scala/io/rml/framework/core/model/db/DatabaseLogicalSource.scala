package io.rml.framework.core.model.db

import io.rml.framework.core.model.{DatabaseSource, LogicalSource, Uri}
import io.rml.framework.core.vocabulary.QueryVoc

case class DatabaseLogicalSource(
                                  source: DatabaseSource,
                                  sqlVersion: String,
                                  query: String
                                ) extends LogicalSource {

  /**
   *
   * @return
   */
  override def iterators: List[String] = List("") // databases have default iterator

  /**
   *
   * @return
   */
  override def referenceFormulation: Uri = Uri(QueryVoc.Class.CSV)
}
