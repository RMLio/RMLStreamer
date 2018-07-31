package io.rml.framework.flink.source

import io.rml.framework.flink.item.Item
import org.apache.flink.api.common.io.DelimitedInputFormat
import org.apache.flink.core.fs.Path

class CSVInputFormat(filePath: String, delimiter: Char = ',', quoteChar: Char = '"', header: Array[String] = Array.empty) extends DelimitedInputFormat[Item](new Path(filePath), null) {


  override def readRecord(reuse: Item, bytes: Array[Byte], offset: Int, numBytes: Int): Item = {

    
    for( index <-  0 until numBytes) {

    }
  }
}
