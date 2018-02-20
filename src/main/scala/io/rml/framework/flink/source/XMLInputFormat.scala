 package io.rml.framework.flink.source

import com.ximpleware.extended.{AutoPilotHuge, VTDGenHuge}
import com.ximpleware.{AutoPilot, VTDGen}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.util.XMLNamespace
import io.rml.framework.shared.RMLException
import org.apache.flink.api.common.io.{GenericInputFormat, NonParallelInput}
import org.apache.flink.core.io.GenericInputSplit
import org.slf4j.LoggerFactory

class XMLInputFormat(path : String, xpath: String) extends GenericInputFormat[Item] with NonParallelInput {

  val LOG = LoggerFactory.getLogger(classOf[XMLInputFormat])

  private var iterator : Iterator[Option[Item]] = _

  override def open(inputSplit: GenericInputSplit): Unit = {
    super.open(inputSplit)

    val namespaces: Map[String, String] =  XMLNamespace.fromFile(path).map(tuple => tuple._1 -> tuple._2).toMap

    val vg = new VTDGenHuge // parser for xml

    if (vg.parseFile(path, true,VTDGenHuge.MEM_MAPPED)) {
      // setting up navigator and autopilot, these are needed to stream through the xml
      val vn = vg.getNav
      val ap = new AutoPilotHuge(vn)
      namespaces.foreach(tuple => {
        ap.declareXPathNameSpace(tuple._1, tuple._2)
      })
      // set the xpath expression
      ap.selectXPath(xpath)
      // create the iterator for the Akka Source
      iterator = XMLIterator(ap, vn, namespaces)
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
      else new EmptyItem
  }

}

class EmptyItem extends Item {
  override def refer(reference: String): Option[String] = None
}
