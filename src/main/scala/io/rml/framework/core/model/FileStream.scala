package io.rml.framework.core.model

case class FileStream( path: String) extends StreamDataSource {
  override def uri: ExplicitNode = {
    Uri(path.hashCode.toHexString)
  }
}
