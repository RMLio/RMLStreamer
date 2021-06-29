package io.rml.framework.flink.bulkwriter

import java.io.OutputStream
import java.util.zip.{ZipEntry, ZipOutputStream}

class ZipBulkWriter(outputStream: OutputStream) extends CompressionBulkWriter {
  compressionStream = new ZipOutputStream(outputStream)
  compressionStream.asInstanceOf[ZipOutputStream].putNextEntry(new ZipEntry("main"))

}
