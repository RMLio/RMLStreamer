package io.rml.framework.flink.source

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

import com.ximpleware.{AutoPilot, VTDNav}
import io.rml.framework.core.internal.Logging
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.xml.XMLItem
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * Custom iterator that is a wrapper around the VTD-XML library for parsing XML
  * with XPath. This can be given to a Akka Source for example.
  * This is a stateful class.
  *
  * @param ap
  * @param vn
  */
class XMLIterator(val ap: AutoPilot, vn: VTDNav) extends Iterator[Option[Item]] with Logging {

  private val documentBuilderFactory = DocumentBuilderFactory.newInstance()
  private val documentBuilder = documentBuilderFactory.newDocumentBuilder()

  private val LOG = LoggerFactory.getLogger(XMLIterator.getClass)

  // flag for indicating if the iterator has to stop or not
  var finished = false

  /**
    * Checks if another element can be taken.
    * @return
    */
  override def hasNext: Boolean = {
    !finished
  }

  /**
    * Takes the next XML element.
    * @return
    */
  override def next(): Option[Item] = {

    /**
      * Quite extensive method due to the very complicated library that is being used.
      */

    // evaluate until the first match
    val node = ap.evalXPath()

    LOG.info("Node = " + node)

    // if node != -1: there is a match
    if(node != -1) {

      LOG.info("RAW STRING?: " + vn.toString(node))

      // get the element string
      val element = vn.toString(node)
      val ap2 = new AutoPilot (vn)
      ap2.selectXPath("@*")

      val attributesMap = new mutable.HashMap[String, String]()
      var i = ap2.evalXPath()

      while(i != -1) {

        val attributeKey = vn.toString(i)
        val attributeValue = vn.toString(i+1)
        attributesMap.put(attributeKey, attributeValue)
        i = ap2.evalXPath()

      }

      val document = documentBuilder.newDocument()
      val firstElement = document.createElement(element)
      attributesMap.foreach(entry => {
        firstElement.setAttribute(entry._1, entry._2)
      })
      document.appendChild(firstElement)

      // map to hold attributes and values of element
      val map = new mutable.HashMap[String, String]()

      // navigate to the first child, if there is one add the
      // attribute and the value to the map
      if(vn.toElement(VTDNav.FIRST_CHILD)) {
        // if first child has a direct value, add to map
        // if not, skip this one
        if(vn.getText != -1) {
          val attribute = vn.toString(vn.getText - 1)
          val value = vn.toString(vn.getText)
          map.put(attribute, value)
          val element = document.createElement(attribute)
          element.appendChild(document.createTextNode(value))
          firstElement.appendChild(element)
        }
      }

      // navigate to the siblings of the first child, if there are:
      // add the attribute and value to the map
      while(vn.toElement(VTDNav.NEXT_SIBLING)) {
        if(vn.getText != -1) {
          // if current sibling has a direct value, add to map
          // if not, skip this one
          val value = vn.toString(vn.getText)
          val attribute = vn.toString(vn.getText - 1)
          map.put(attribute, value)
          val element = document.createElement(attribute)
          element.appendChild(document.createTextNode(value))
          firstElement.appendChild(element)
        }
      }

      map.foreach(item => println(item))

      // navigate back to the parent
      vn.toElement(VTDNav.PARENT)

      // return the item
      //Some(item)
      import javax.xml.transform.OutputKeys
      import javax.xml.transform.TransformerFactory
      import javax.xml.transform.dom.DOMSource
      import javax.xml.transform.stream.StreamResult
      import java.io.StringWriter
      val xmlString = try {
        val sw = new StringWriter
        val tf = TransformerFactory.newInstance
        val transformer = tf.newTransformer
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
        transformer.setOutputProperty(OutputKeys.METHOD, "xml")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.transform(new DOMSource(document), new StreamResult(sw))
        sw.toString
      } catch {
        case ex: Exception =>
          throw new RuntimeException("Error converting to String", ex)
      }

      Some(XMLItem.fromString(xmlString))

    } else {
      LOG.info("It's done.. without errors though.")
      // no elements left, set finished flag to true and return None
      finished = true
      None
    }

  }

}

object XMLIterator {
  def apply(ap: AutoPilot, vn: VTDNav): XMLIterator = new XMLIterator(ap, vn)
}


