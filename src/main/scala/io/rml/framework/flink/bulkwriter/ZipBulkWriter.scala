package io.rml.framework.flink.bulkwriter

import java.io.OutputStream
import java.util.zip.{ZipEntry, ZipOutputStream}

class ZipBulkWriter(outputStream: OutputStream) extends CompressionBulkWriter {
  val zipOutputStream = new ZipOutputStream(outputStream)
  compressionStream = zipOutputStream
  compressionStream.asInstanceOf[ZipOutputStream].putNextEntry(new ZipEntry("main"))

  override def finish(): Unit = {
    zipOutputStream.finish()
  }
}
