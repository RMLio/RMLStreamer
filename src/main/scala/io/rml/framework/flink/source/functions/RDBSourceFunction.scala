package io.rml.framework.flink.source.functions

import be.ugent.idlab.knows.dataio.access.Access
import be.ugent.idlab.knows.dataio.source.CSVSource
import io.rml.framework.core.internal.Logging
import io.rml.framework.flink.source.throwaway.SerializableIterator
import org.apache.flink.streaming.api.functions.source.SourceFunction

/**
 * Implementation of getting data from RDB
 */
class RDBSourceFunction(access: Access) extends SourceFunction[CSVSource] with Logging with Serializable {

  val iterator = new SerializableIterator(access)
  var running = true

  override def run(sourceContext: SourceFunction.SourceContext[CSVSource]): Unit = {

    while (running && iterator.hasNext) {
      sourceContext.getCheckpointLock.synchronized {
        val item = this.iterator.next()
        logInfo(item.toString)
        sourceContext.collect(item.asInstanceOf[CSVSource])
      }
    }
  }

  override def cancel(): Unit = {
    running = false
  }
}
