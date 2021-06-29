package io.rml.framework.flink.bulkwriter

import org.apache.flink.api.common.serialization.BulkWriter

import java.io.OutputStream

class CompressionBulkWriter extends BulkWriter[String] {
  protected var compressionStream: OutputStream = _

  override def addElement(element: String): Unit = {
    compressionStream.write(element.getBytes())
    compressionStream.write('\n')
  }

  override def flush(): Unit = {
    compressionStream.flush()
  }

  override def finish(): Unit = {}
}
