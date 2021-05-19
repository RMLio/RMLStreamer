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
package io.rml.framework.core.item.xml

import com.ximpleware.extended.{AutoPilotHuge, VTDNavHuge}
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.item.Item
import org.slf4j.LoggerFactory

import java.nio.charset.{Charset, StandardCharsets}

/**
  * Custom iterator that is a wrapper around the VTD-XML library for parsing XML
  * with XPath. This can be given to a Akka Source for example.
  * This is a stateful class.
  *
  * @param ap
  * @param vn
  *
  * TODO: move to other package
  */
class XMLIterator(val ap: AutoPilotHuge, vn: VTDNavHuge, namespaces: Map[String, String], xPath: String) extends Iterator[Option[Item]] with Logging {

  private val LOG = LoggerFactory.getLogger(XMLIterator.getClass)
  private val xmlBuffer = vn.getXML

  // flag for indicating if the iterator has to stop or not
  var finished = false

  /**
    * Checks if another element can be taken.
    *
    * @return
    */
  override def hasNext: Boolean = {
    !finished
  }

  /**
    * Takes the next XML element.
    *
    * @return
    */
  override def next(): Option[Item] = {

    /**
      * Quite extensive method due to the very complicated library that is being used.
      */

    // evaluate until the first match
    val node = ap.evalXPath()

    val fragment = vn.getElementFragment
    val fragmentOffset = fragment(0);
    val fragmentLength = fragment(1).toInt

    if (isDebugEnabled) logDebug("Node = " + node)

    // if node != -1: there is a match
    if (node != -1) {
      if (isDebugEnabled) logDebug("RAW STRING?: " + vn.toString(node))

      val fragmentBytes = new Array[Byte](fragmentLength)

      // XMLMemMappedBuffer does not yet support the more efficient getBytes() method
      // the byteAt() method is supported, so bytes have to be copied inefficiently using the byteAt() method
      var fragmentByteIndex = 0
      while (fragmentByteIndex < fragmentLength) {
        fragmentBytes(fragmentByteIndex) = xmlBuffer.byteAt(fragmentOffset + fragmentByteIndex)
        fragmentByteIndex += 1
      }

      val encoding = vn.getEncoding match {
        case VTDNavHuge.FORMAT_ASCII => StandardCharsets.US_ASCII
        case VTDNavHuge.FORMAT_ISO_8859_1 => StandardCharsets.ISO_8859_1
        case VTDNavHuge.FORMAT_UTF8 => StandardCharsets.UTF_8
        case VTDNavHuge.FORMAT_UTF_16BE => StandardCharsets.UTF_16BE
        case VTDNavHuge.FORMAT_UTF_16LE => StandardCharsets.UTF_16LE
        case VTDNavHuge.FORMAT_ISO_8859_2 => Charset.forName("ISO-8859-2")
        case VTDNavHuge.FORMAT_ISO_8859_3 => Charset.forName("ISO-8859-3")
        case VTDNavHuge.FORMAT_ISO_8859_4 => Charset.forName("ISO-8859-4")
        case VTDNavHuge.FORMAT_ISO_8859_5 => Charset.forName("ISO-8859-5")
        case VTDNavHuge.FORMAT_ISO_8859_6 => Charset.forName("ISO-8859-6")
        case VTDNavHuge.FORMAT_ISO_8859_7 => Charset.forName("ISO-8859-7")
        case VTDNavHuge.FORMAT_ISO_8859_8 => Charset.forName("ISO-8859-8")
        case VTDNavHuge.FORMAT_ISO_8859_9 => Charset.forName("ISO-8859-9")
        case VTDNavHuge.FORMAT_ISO_8859_10 => Charset.forName("ISO-8859-10")
        case VTDNavHuge.FORMAT_ISO_8859_11 => Charset.forName("ISO-8859-11")
        case VTDNavHuge.FORMAT_ISO_8859_12 => Charset.forName("ISO-8859-12")
        case VTDNavHuge.FORMAT_ISO_8859_13 => Charset.forName("ISO-8859-13")
        case VTDNavHuge.FORMAT_ISO_8859_14 => Charset.forName("ISO-8859-14")
        case VTDNavHuge.FORMAT_ISO_8859_15 => Charset.forName("ISO-8859-15")
        case VTDNavHuge.FORMAT_ISO_8859_16 => Charset.forName("ISO-8859-16")
        case VTDNavHuge.FORMAT_WIN_1250 => Charset.forName("windows-1250")
        case VTDNavHuge.FORMAT_WIN_1251 => Charset.forName("windows-1251")
        case VTDNavHuge.FORMAT_WIN_1252 => Charset.forName("windows-1252")
        case VTDNavHuge.FORMAT_WIN_1253 => Charset.forName("windows-1253")
        case VTDNavHuge.FORMAT_WIN_1254 => Charset.forName("windows-1254")
        case VTDNavHuge.FORMAT_WIN_1255 => Charset.forName("windows-1255")
        case VTDNavHuge.FORMAT_WIN_1256 => Charset.forName("windows-1256")
        case VTDNavHuge.FORMAT_WIN_1257 => Charset.forName("windows-1257")
        case VTDNavHuge.FORMAT_WIN_1258 => Charset.forName("windows-1258")
        case _ => StandardCharsets.UTF_8
      }

      val result = Some(XMLItem.fromString(new String(fragmentBytes, encoding), namespaces, xPath))
      result
    } else {
      if (isDebugEnabled) logDebug("No match found => done.")
      // no elements left, set finished flag to true and return None
      finished = true
      None
    }

  }

}

object XMLIterator {
  def apply(ap: AutoPilotHuge, vn: VTDNavHuge, namespaces: Map[String, String], xPath: String): XMLIterator = new XMLIterator(ap, vn, namespaces, xPath)
}


