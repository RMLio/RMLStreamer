package io.rml.framework.core.model

case class Parameters(map: Map[Uri, String]) {

  def getParam(uri: Uri): Option[String] = {
    map.get(uri)
  }

}
