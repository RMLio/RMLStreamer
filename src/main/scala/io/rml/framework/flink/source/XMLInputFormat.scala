package io.rml.framework.flink.source

import com.ximpleware.{AutoPilot, VTDGen}
import io.rml.framework.flink.item.Item
import io.rml.framework.shared.RMLException
import org.apache.flink.api.common.io.{GenericInputFormat, NonParallelInput}
import org.apache.flink.core.io.GenericInputSplit
import org.slf4j.LoggerFactory

class XMLInputFormat(path : String, xpath: String) extends GenericInputFormat[Item] with NonParallelInput {

  val LOG = LoggerFactory.getLogger(classOf[XMLInputFormat])

  private var iterator : Iterator[Option[Item]] = _

  override def open(inputSplit: GenericInputSplit): Unit = {
    super.open(inputSplit)
    val vg = new VTDGen // parser for xml
    if (vg.parseFile(path, true)) {
      // setting up navigator and autopilot, these are needed to stream through the xml
      val vn = vg.getNav
      val ap = new AutoPilot(vn)
      // set the xpath expression
      ap.selectXPath(xpath)
      // create the iterator for the Akka Source
      iterator = XMLIterator(ap, vn)
      LOG.info("Run the XML source!")
    } else{
      throw new RMLException("Can't parse XML with VTD.")
    }
  }

  override def reachedEnd() = !iterator.hasNext

  override def nextRecord(reuse: Item) = {
      LOG.info("Going for the next!")
      val next = iterator.next()
      if(next.isDefined) next.get
      else new Item{
        override def refer(reference: String): None.type = None
      }
  }

}
