package io.rml.framework.flink.item.xml

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}

import io.rml.framework.flink.item.Item
import org.apache.commons.io.IOUtils
import org.w3c.dom.{Document, NodeList}

import scala.util.control.NonFatal

class XMLItem(xml: Document) extends Item {

  private val documentBuilderFactory = DocumentBuilderFactory.newInstance()
  private val documentBuilder = documentBuilderFactory.newDocumentBuilder()
  private val xPath = XPathFactory.newInstance().newXPath()

  override def refer(reference: String) : Option[String] = {
    // the node name is added as a little hack such that the node itself does not need to be in the reference (e.g. "/note/@day" vs "@day")
    val nodes = try {xPath.compile("/" + xml.getFirstChild.getNodeName + "/" + reference).evaluate(xml, XPathConstants.NODESET).asInstanceOf[NodeList]}
                catch { case NonFatal(e) => return None }

    if(nodes.getLength > 0) {
      val text = nodes.item(0).getTextContent
      if(text == null) None
      Some(text)
    } else None
  }

}

object XMLItem {



  def fromString(xml:String): XMLItem = {
    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    val documentBuilder = documentBuilderFactory.newDocumentBuilder()
    val xPath = XPathFactory.newInstance().newXPath()
    val document: Document = documentBuilder.parse(IOUtils.toInputStream(xml))
    new XMLItem(document)
  }

  def fromStringOptionable(xml: String): Option[XMLItem] = {
    try {
      Some(fromString(xml))
    } catch {
      case NonFatal(e) => None
    }
  }

}
