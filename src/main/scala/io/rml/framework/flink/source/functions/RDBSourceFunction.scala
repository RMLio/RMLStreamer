package io.rml.framework.flink.source.functions

import be.ugent.idlab.knows.dataio.access.Access
import be.ugent.idlab.knows.dataio.iterators.CSVSourceIterator
import be.ugent.idlab.knows.dataio.source.CSVSource
import io.rml.framework.core.internal.Logging
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}

/**
 * Implementation of getting data from RDB
 */
class RDBSourceFunction(access: Access) extends RichSourceFunction[CSVSource] with Logging with Serializable {

//  @transient
//  val iterator: CSVSourceIterator = new CSVSourceIterator()
//  iterator.open(access)
  var running = true

  override def run(sourceContext: SourceFunction.SourceContext[CSVSource]): Unit = {
    val iterator = new CSVSourceIterator()
    iterator.open(access)
    while (running && iterator.hasNext) {
      sourceContext.getCheckpointLock.synchronized {
        val item = iterator.next()
        logInfo(item.toString)
        sourceContext.collect(item.asInstanceOf[CSVSource])
      }
    }
  }

  override def cancel(): Unit = {
    running = false
  }
}
