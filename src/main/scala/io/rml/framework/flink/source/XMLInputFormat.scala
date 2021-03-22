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
package io.rml.framework.flink.source

import com.ximpleware.extended.{AutoPilotHuge, VTDGenHuge}
import io.rml.framework.core.item.xml.XMLIterator
import io.rml.framework.core.item.{EmptyItem, Item}
import io.rml.framework.core.util.XMLNamespace
import io.rml.framework.shared.RMLException
import org.apache.flink.api.common.io.{GenericInputFormat, NonParallelInput}
import org.apache.flink.core.io.GenericInputSplit
import org.slf4j.LoggerFactory

class XMLInputFormat(path: String, xpath: String) extends GenericInputFormat[Item] with NonParallelInput {

  val LOG = LoggerFactory.getLogger(classOf[XMLInputFormat])

  private var iterator: Iterator[Option[Item]] = _

  override def open(inputSplit: GenericInputSplit): Unit = {
    super.open(inputSplit)

    val namespaces: Map[String, String] = XMLNamespace.fromFile(path).map(tuple => tuple._1 -> tuple._2).toMap

    val vg = new VTDGenHuge // parser for xml

    if (vg.parseFile(path, true, VTDGenHuge.MEM_MAPPED)) {
      // setting up navigator and autopilot, these are needed to stream through the xml
      val vn = vg.getNav
      val ap = new AutoPilotHuge(vn)
      namespaces.foreach(tuple => {
        ap.declareXPathNameSpace(tuple._1, tuple._2)
      })
      // set the xpath expression
      ap.selectXPath(xpath)
      // create the iterator for the Akka Source
      iterator = XMLIterator(ap, vn, namespaces, xpath)
      LOG.info("Run the XML source!")
    } else {
      throw new RMLException("Can't parse XML with VTD.")
    }
  }

  override def reachedEnd() = !iterator.hasNext

  override def nextRecord(reuse: Item) = {
    LOG.info("Going for the next!")
    val next = iterator.next()
    if (next.isDefined) next.get
    else new EmptyItem
  }

}


