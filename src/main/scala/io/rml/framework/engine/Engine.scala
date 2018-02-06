/*
 * Copyright (c) 2017 Ghent University - imec
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.rml.framework.engine

import io.rml.framework.core.extractors.std.TermMapExtractor
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.Literal
import io.rml.framework.core.model.rdf.RDFTriple
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.sink.FlinkRDFTriple

/**
  * Created by wmaroy on 22.08.17.
  */
trait Engine[T] extends Serializable {

  def process(item: T) : List[FlinkRDFTriple]

}

object Engine extends Logging {


  /**
    * Retrieve reference included in a template from the given item.
    * @param template
    * @param item
    * @return
    */
  def processTemplate(template: Literal, item: Item) : Option[String] = {
    val regex = "(\\{[^\\{\\}]*\\})".r
    val result = regex.replaceAllIn(template.value, m => {
      val reference = removeBrackets(m.toString())
      val referred = item.refer(reference)
      if(referred.isDefined) referred.get
      else m.toString()
    })
    if(regex.findFirstIn(result).isEmpty) Some(result) else None
  }

  /**
    * Retrieve the value of a reference from a given item.
    * @param reference
    * @param item
    * @return
    */
  def processReference(reference: Literal, item: Item) : Option[String] = {
    item.refer(reference.toString)
  }

  /**
    * Private util method for removing brackets from a template string.
    * @param s
    * @return
    */
  private def removeBrackets(s : String) : String = {
    s.replace("{","")
     .replace("}","")
  }

}
