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

package io.rml.framework.engine

import be.ugent.idlab.knows.functions.agent.Agent
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.item.Item
import io.rml.framework.core.model.rdf.SerializableRDFQuad
import io.rml.framework.core.model.{Literal, Uri}

import java.util.regex.Pattern
import scala.collection.mutable

/**
  * Created by wmaroy on 22.08.17.
  */
trait Engine[T] extends Serializable {

  def process(item: T, functionAgent: Agent): List[SerializableRDFQuad]

}

object Engine extends Logging {


  /**
    * Retrieve reference included in a template from the given item.
    *
    * @param template
    * @param item
    * @return
    */
  def processTemplate(template: Literal, item: Item, encode: Boolean = false): Option[Iterable[String]] = {
    val regex = "(\\{[^\\{\\}]*\\})".r
    val replaced = template.value.replaceAll("\\$", "#")
    val result: mutable.Queue[String] = mutable.Queue.empty[String]
    val matches = regex.findAllMatchIn(replaced).toList


    val tuples = for {
      m <- matches
      sanitizedRef = removeBrackets(m.toString()).replaceAll("#", "\\$")
      optReferred = if (encode) item.refer(sanitizedRef).map(lString => lString.map(el => Uri.encode(el))) else item.refer(sanitizedRef)
      quotedSanitizedRef = Pattern.quote(sanitizedRef)
      if optReferred.isDefined
    } yield (optReferred, quotedSanitizedRef)


    if (tuples.size != matches.size) {
      None
    } else {
      for ((optReferred, quotedSanitizedRef) <- tuples) {

        optReferred.foreach(refList => {

          // Using fifo queue to create a list of template string with the combination of referenced resources
          if (result.isEmpty) {
            //Used string template since we still have to escape the curly brackets
            result ++= refList.map(referred => replaced.replaceAll(s"\\{$quotedSanitizedRef\\}", referred))
          } else {

            // Previously edited templates need to be reused for combination with new referenced resources
            var count = 0
            val maxLen = result.length
            while (count != maxLen) {
              val candid = result.dequeue()
              result ++= refList.map(referred => candid.replaceAll(s"\\{$quotedSanitizedRef\\}", referred))

              count += 1
            }


          }
        })
      }

      // when all template-references are replaced with their corresponding value,
      // any escaped curly bracket can be unescaped.
      // e.g. \{ -> {
      Some(result.map(unescapeCurlyBrackets))

    }
  }

  /**
    * Retrieve the value of a reference from a given item.
    *
    * @param reference
    * @param item
    * @return
    */
  def processReference(reference: Literal, item: Item): Option[List[String]] = {
    item.refer(reference.value)
  }

  /**
    * Private util method for removing brackets from a template string.
    *
    * @param s
    * @return
    */
  private def removeBrackets(s: String): String

  = {
    s.replace("{", "")
      .replace("}", "")
  }

  /**
   * Unescapes curly brackets
   * @param s
   * @return
   */
  private def unescapeCurlyBrackets(s: String) : String = {
    s
      .replaceAllLiterally("""\{""", "{")
      .replaceAllLiterally("""\}""", "}")
  }

}
