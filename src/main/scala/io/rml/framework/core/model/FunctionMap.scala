package io.rml.framework.core.model

case class FunctionMap(identifier: String, functionValue: TripleMap) extends TermMap {

  /**
    *
    * @return
    */
  override def constant = None

  /**
    *
    * @return
    */
  override def reference = None

  /**
    *
    * @return
    */
  override def template = None

  /**
    *
    * @return
    */
  override def termType = None

}
