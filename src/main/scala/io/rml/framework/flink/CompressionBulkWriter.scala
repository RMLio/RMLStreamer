package io.rml.framework.flink

import io.rml.framework.flink.CompressionFormat.{CompressionFormat, GZIP, ZIP}
import org.apache.flink.api.common.serialization.BulkWriter

import java.io.OutputStream
import java.util.zip.GZIPOutputStream

object CompressionFormat extends Enumeration {
  type CompressionFormat = Value
  val GZIP, ZIP, TARGZIP, TARXZ = Value
}

class CompressionBulkWriter(compressionFormat: CompressionFormat, outputStream: OutputStream) extends BulkWriter[String] {
  private val stream = compressionFormat match {
    case GZIP => new GZIPOutputStream(outputStream, true)
    case _ => outputStream
  }

  override def addElement(element: String): Unit = {
    stream.write(element.getBytes())
    stream.write('\n')
  }

  override def flush(): Unit = {
    stream.flush()
  }

  override def finish(): Unit = {
    compressionFormat match {
      case GZIP => stream.asInstanceOf[GZIPOutputStream].finish()
      case _ =>
    }
  }
}
