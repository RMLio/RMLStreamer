package io.rml.framework.flink.bulkwriter

import java.io.OutputStream
import java.util.zip.GZIPOutputStream

class GZIPBulkWriter(outputStream: OutputStream) extends CompressionBulkWriter {
  val gzipOutputStream = new GZIPOutputStream(outputStream, true)
  compressionStream = gzipOutputStream

  override def finish(): Unit = {
    gzipOutputStream.finish()
  }
}
