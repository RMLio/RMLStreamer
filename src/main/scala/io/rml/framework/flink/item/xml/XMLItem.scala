package io.rml.framework.flink.item.xml

import java.io.{ByteArrayInputStream, InputStreamReader}
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}

import com.ximpleware.extended.{AutoPilotHuge, VTDGenHuge, XMLBuffer}
import io.netty.util.CharsetUtil
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.source.XMLIterator
import io.rml.framework.flink.util.XMLNamespace
import org.apache.commons.io.IOUtils
import org.w3c.dom.{Document, NodeList}

import scala.util.control.NonFatal
import scala.xml.{PrettyPrinter, XML}

class XMLItem(xml: Document, namespaces: Map[String, String]) extends Item {

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

  override def refer(reference: String): Option[List[String]] = {

    val xpath = "/" + xml.getFirstChild.getNodeName + "/" + reference
    // the node name is added as a little hack such that the node itself does not need to be in the reference (e.g. "/note/@day" vs "@day")
    val nodes =
      try {
        xPath.compile(xpath).evaluate(xml, XPathConstants.NODESET).asInstanceOf[NodeList]
      }
      catch {
        case NonFatal(e) => return None
      }

    if (nodes.getLength > 0) {
      val text = nodes.item(0).getTextContent.trim
      if (text == null) None
      Some(List(text))
    } else None
  }

  override def toString: String = {
    import java.io.StringWriter
    import javax.xml.transform.dom.DOMSource
    import javax.xml.transform.stream.StreamResult
    import javax.xml.transform.{OutputKeys, TransformerFactory}
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


  def getNSpacesFromString(xml: String): Map[String, String] = {
    val inputStream = new ByteArrayInputStream(xml.getBytes(CharsetUtil.UTF_8))
    val reader = new InputStreamReader(inputStream)
    try {
      val namespace = XMLNamespace.fromStreamReader(reader)
      namespace.map(tuple => tuple._1 -> tuple._2).toMap
    } finally {
      if (reader != null) reader.close()
    }
  }

  // Since this is being used in other working parts of the code, it won't be refactored for now 20/7/18
  def fromString(xml: String, namespaces: Map[String, String] = Map()): XMLItem = {
    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    documentBuilderFactory.setNamespaceAware(true)
    val documentBuilder = documentBuilderFactory.newDocumentBuilder()

    val document: Document = documentBuilder.parse(IOUtils.toInputStream(xml))

    new XMLItem(document, namespaces)

  }

  def fromStringOptionable(orgXml: String, xpath: String): Option[Array[Item]] = {
    try {


      val namespaces = getNSpacesFromString(orgXml)
      val xml = orgXml
      val vg = new VTDGenHuge

      val bytes = xml.getBytes()
      val xmlBuffer = new XMLBuffer(bytes)
      // parse the xml string as bytes using VTD5

      vg.setDoc(xmlBuffer)
      vg.parse(true)
      val vn = vg.getNav
      val ap = new AutoPilotHuge(vn)
      namespaces.foreach(tuple => {
        ap.declareXPathNameSpace(tuple._1, tuple._2)
      })
      // set the xpath expression
      ap.selectXPath(xpath)



      val result = XMLIterator(ap,vn,namespaces).flatten.toArray
      Some(result)
    } catch {
      case NonFatal(e) => None
    }
  }

}
