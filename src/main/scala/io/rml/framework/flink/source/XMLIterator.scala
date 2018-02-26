package io.rml.framework.flink.source

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

import com.ximpleware.extended.{AutoPilotHuge, VTDNavHuge}
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
class XMLIterator(val ap: AutoPilotHuge, vn: VTDNavHuge, namespaces: Map[String,String]) extends Iterator[Option[Item]] with Logging {

  private val documentBuilderFactory = DocumentBuilderFactory.newInstance()
  documentBuilderFactory.setNamespaceAware(true)
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
      val ap2 = new AutoPilotHuge(vn)
      ap2.selectXPath("@*")

      val attributesMap = new mutable.HashMap[String, String]()
      var i = ap2.evalXPath()

      while (i != -1) {

        val attributeKey = vn.toString(i)
        val attributeValue = vn.toString(i + 1)
        attributesMap.put(attributeKey, attributeValue)
        i = ap2.evalXPath()

      }

      val document = documentBuilder.newDocument()

      // check for namespaces
      val firstElement = if (element.contains(':')) {
        val regex = "(.*):".r
        val matches = regex.findAllIn(element).matchData map {
          m => m.group(1)
        }
        val namespaceKey = matches.toList.head
        val namespace = namespaces.get(namespaceKey).orNull
        document.createElementNS(namespace, element)
      } else document.createElement(element)

      attributesMap.foreach(entry => {
        firstElement.setAttribute(entry._1, entry._2)
      })

      document.appendChild(firstElement)

      // map to hold attributes and values of element
      val map = new mutable.HashMap[String, String]()

      // navigate to the first child
      if(vn.toElement(VTDNav.FIRST_CHILD)) {
        // if first child has a direct value, add to map
        // if not, skip this one
        val node = vn.toString(vn.getCurrentIndex)
        if(vn.getText != -1) {
          val attribute = vn.toString(vn.getText - 1)
          val value = vn.toString(vn.getText)
          map.put(attribute, value)
           //document.createElement(attribute)
           val element = if (node.contains(':')) {
            val regex = "(.*):".r
            val matches = regex.findAllIn(node).matchData map {
              m => m.group(1)
            }
            val namespaceKey = matches.toList.head
            val namespace = namespaces.get(namespaceKey).orNull
            document.createElementNS(namespace, node)} else document.createElement(node)

          element.appendChild(document.createTextNode(value))
          firstElement.appendChild(element)
        }

        // navigate to the siblings of the first child, if there are:
        // add the attribute and value to the map
        while(vn.toElement(VTDNav.NEXT_SIBLING)) {
          val node = vn.toString(vn.getCurrentIndex)
          if(vn.getText != -1) {
            // if current sibling has a direct value, add to map
            // if not, skip this one
            val value = vn.toString(vn.getText)
            val attribute = vn.toString(vn.getText - 1)
            val element = if (node.contains(':')) {
              val regex = "(.*):".r
              val matches = regex.findAllIn(node).matchData map {
                m => m.group(1)
              }
              val namespaceKey = matches.toList.head
              val namespace = namespaces.get(namespaceKey).orNull
              document.createElementNS(namespace, node)}
            else document.createElement(node)
            element.appendChild(document.createTextNode(value))
            firstElement.appendChild(element)
          }

        }

        // navigate back to the parent
        vn.toElement(VTDNav.PARENT)

      }


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

      val result = Some(XMLItem.fromString(xmlString, namespaces))
      result
    } else {
      LOG.info("It's done.. without errors though.")
      // no elements left, set finished flag to true and return None
      finished = true
      None
    }

  }

}

object XMLIterator {
  def apply(ap: AutoPilotHuge, vn: VTDNavHuge, namespaces: Map[String,String]): XMLIterator = new XMLIterator(ap, vn, namespaces)
}


