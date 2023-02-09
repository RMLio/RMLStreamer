package io.rml.framework.flink.source.functions

import be.ugent.idlab.knows.dataio.access.{Access, RDBAccess}
import be.ugent.idlab.knows.dataio.iterators.CSVSourceIterator
import be.ugent.idlab.knows.dataio.source.CSVSource
import io.rml.framework.core.internal.Logging
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}

/**
 * Implementation of getting data from RDB
 */
class RDBSourceFunction(access: RDBAccess) extends RichSourceFunction[CSVSource] with Logging with Serializable {
  var running = true

  override def run(sourceContext: SourceFunction.SourceContext[CSVSource]): Unit = {
    val iterator = new CSVSourceIterator()
    iterator.open(access)
    while (running && iterator.hasNext) {
      sourceContext.getCheckpointLock.synchronized {
        sourceContext.collect(iterator.next().asInstanceOf[CSVSource])
      }
    }
  }

  override def cancel(): Unit = {
    running = false
  }
}
