package io.rml.framework.flink.item.xml

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}

import io.rml.framework.flink.item.Item
import org.apache.commons.io.IOUtils
import org.w3c.dom.{Document, NodeList}

class XMLItem(xml: Document) extends Item {

  override def refer(reference: String) : Option[String] = {
    val nodes = XMLItem.xPath.compile(reference).evaluate(xml, XPathConstants.NODESET).asInstanceOf[NodeList]
    if(nodes.getLength > 0) {
      val text = nodes.item(0).getTextContent
      if(text == null) None
      Some(text)
    } else None
  }

}

object XMLItem {

  private val documentBuilderFactory = DocumentBuilderFactory.newInstance()
  private val documentBuilder = documentBuilderFactory.newDocumentBuilder()
  private val xPath = XPathFactory.newInstance().newXPath()

  def fromString(xml:String): XMLItem = {
    val document: Document = documentBuilder.parse(IOUtils.toInputStream(xml))
    new XMLItem(document)
  }

}
