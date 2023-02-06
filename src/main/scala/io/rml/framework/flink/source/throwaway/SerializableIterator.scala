package io.rml.framework.flink.source.throwaway

import be.ugent.idlab.knows.dataio.access.Access
import be.ugent.idlab.knows.dataio.iterators.CSVSourceIterator
import be.ugent.idlab.knows.dataio.source.Source

class SerializableIterator(val access: Access) extends Serializable with Iterator[Source] {

  val iterator = new CSVSourceIterator()
  iterator.open(access)
  override def hasNext: Boolean = iterator.hasNext

  override def next(): Source = iterator.next()
}
