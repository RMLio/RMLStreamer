package io.rml.framework.flink.item.xml

import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}

import io.rml.framework.flink.item.Item
import org.apache.commons.io.IOUtils
import org.w3c.dom.{Document, NodeList}

import scala.util.control.NonFatal

class XMLItem(xml: Document, namespaces: Map[String,String]) extends Item {

  private val xPath = XPathFactory.newInstance().newXPath()

  xPath.setNamespaceContext(new NamespaceContext() {
    def getNamespaceURI(prefix: String): String = {
      if (prefix == null) throw new NullPointerException("Null prefix")
      val namespaceUri = namespaces.get(prefix).orNull
      namespaceUri
    } // This method isn't necessary for XPath processing.
    def getPrefix(uri: String) = throw new UnsupportedOperationException // This method isn't necessary for XPath processing either.
    def getPrefixes(uri: String) = throw new UnsupportedOperationException
  })
  private val content = toString()

  override def refer(reference: String) : Option[String] = {
    val xpath = "/" + xml.getFirstChild.getNodeName + "/" + reference
    // the node name is added as a little hack such that the node itself does not need to be in the reference (e.g. "/note/@day" vs "@day")
    val nodes =
      try {
        xPath.compile(xpath).evaluate(xml, XPathConstants.NODESET).asInstanceOf[NodeList]}
      catch {
        case NonFatal(e) => return None
      }

    if(nodes.getLength > 0) {
      val text = nodes.item(0).getTextContent.trim
      if(text == null) None
      Some(text)
    } else None
  }

  override def toString : String = {
    import javax.xml.transform.OutputKeys
    import javax.xml.transform.TransformerFactory
    import javax.xml.transform.dom.DOMSource
    import javax.xml.transform.stream.StreamResult
    import java.io.StringWriter
    val tf = TransformerFactory.newInstance
    val transformer = tf.newTransformer
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    val writer = new StringWriter
    transformer.transform(new DOMSource(xml), new StreamResult(writer))
    val output = writer.getBuffer.toString.replaceAll("\n|\r", "")
    output
  }

}

object XMLItem {



  def fromString(xml:String, namespaces : Map[String,String] = Map()): XMLItem = {
    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    documentBuilderFactory.setNamespaceAware(true)
    val documentBuilder = documentBuilderFactory.newDocumentBuilder()

    val document: Document = documentBuilder.parse(IOUtils.toInputStream(xml))
    new XMLItem(document, namespaces)
  }

  def fromStringOptionable(xml: String): Option[XMLItem] = {
    try {
      Some(fromString(xml))
    } catch {
      case NonFatal(e) => None
    }
  }

}
