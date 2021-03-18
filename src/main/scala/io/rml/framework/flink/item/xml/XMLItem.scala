/**
  * MIT License
  *
  * Copyright (C) 2017 - 2020 RDF Mapping Language (RML)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  **/

package io.rml.framework.flink.item.xml

import com.ximpleware.extended.{AutoPilotHuge, VTDGenHuge, XMLBuffer}
import io.rml.framework.core.internal.Logging
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.item.xml.XMLItem.documentToString
import io.rml.framework.flink.source.{XMLIterator, XMLStream}
import io.rml.framework.flink.util.XMLNamespace
import org.apache.commons.io.IOUtils
import org.w3c.dom.{Document, NodeList}

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.nio.charset.StandardCharsets
import javax.xml.namespace.NamespaceContext
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}
import scala.util.control.NonFatal
//import scala.xml.{PrettyPrinter, XML}

class XMLItem(xml: Document, namespaces: Map[String, String], val tag: String) extends Item {

  @transient private lazy val xPath = XPathFactory.newInstance().newXPath()

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
      var results: List[String] = List.empty
      for (i <- 0 until nodes.getLength) {
        results = nodes.item(i).getTextContent.trim :: results
      }
      if (results.isEmpty) None
      Some(results)
    } else None
  }

  override def toString: String = documentToString(xml)

}

object XMLItem extends Logging {


  def getNSpacesFromString(xml: String): Map[String, String] = {
    val inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
    val reader = new InputStreamReader(inputStream)
    try {
      val namespace = XMLNamespace.fromStreamReader(reader)
      namespace.map(tuple => tuple._1 -> tuple._2).toMap
    } finally {
      if (reader != null) reader.close()
    }
  }

  // Since this is being used in other working parts of the code, it won't be refactored for now 20/7/18
  def fromString(xml: String, namespaces: Map[String, String] = Map(), xpath:String): XMLItem = {
    val documentBuilderFactory = DocumentBuilderFactory.newInstance()
    documentBuilderFactory.setNamespaceAware(false)
    var documentBuilder = documentBuilderFactory.newDocumentBuilder()
    var document: Document = documentBuilder.parse(IOUtils.toInputStream(xml, StandardCharsets.UTF_8))

    // add namespaces to root element
    val documentRoot = document.getDocumentElement
    for ((namespacePrefix, namespaceURI) <- namespaces) {
      documentRoot.setAttributeNS("http://www.w3.org/2000/xmlns/", s"xmlns:$namespacePrefix", namespaceURI)
    }

    // parse document again, now namespace aware
    // this is a workaround since there does not seem to be a straightforward way to pass namespaces to the parser
    documentBuilderFactory.setNamespaceAware(true)
    documentBuilder = documentBuilderFactory.newDocumentBuilder()
    document = documentBuilder.parse(IOUtils.toInputStream(documentToString(document), StandardCharsets.UTF_8))

    val tag = xpath match {
      case XMLStream.DEFAULT_PATH_OPTION => ""
      case _ => xpath
    }

    new XMLItem(document, namespaces, tag)

  }

  def fromStringOptionable(orgXml: String, iterators: Iterable[String]): List[Item] = {
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

      val resultingList: List[Item] = iterators.flatMap { xpath =>

        try {
          // set the xpath expression
          ap.selectXPath(xpath)
          val result = XMLIterator(ap, vn, namespaces, xpath).flatten
          Some(result)

        } catch {
          case NonFatal(e) => logError(s"Error while parsing XML:\n$xml", e); None
        }
      }
        .flatten
        .toList

      resultingList

    } catch {
      case NonFatal(e) => List()
    }
  }

  def documentToString(document: Document) : String = {
    import java.io.StringWriter
    import javax.xml.transform.dom.DOMSource
    import javax.xml.transform.stream.StreamResult
    import javax.xml.transform.{OutputKeys, TransformerFactory}
    val tf = TransformerFactory.newInstance
    val transformer = tf.newTransformer
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    val writer = new StringWriter
    transformer.transform(new DOMSource(document), new StreamResult(writer))
    val output = writer.getBuffer.toString.replaceAll("[\n\r]", "")
    output
  }
}
