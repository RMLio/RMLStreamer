package io.rml.framework.flink.bulkwriter

import java.io.OutputStream
import java.util.zip.GZIPOutputStream

class GZIPBulkWriter(outputStream: OutputStream) extends CompressionBulkWriter {
  compressionStream = new GZIPOutputStream(outputStream, true)
}
