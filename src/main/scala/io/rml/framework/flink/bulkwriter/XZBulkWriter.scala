package io.rml.framework.flink.bulkwriter

import org.tukaani.xz.{LZMA2Options, XZOutputStream}

import java.io.OutputStream

class XZBulkWriter(outputStream: OutputStream) extends CompressionBulkWriter {
  val xzOutputStream = new XZOutputStream(outputStream, new LZMA2Options())
  compressionStream = xzOutputStream

  override def finish(): Unit = {
    xzOutputStream.finish()
  }
}
