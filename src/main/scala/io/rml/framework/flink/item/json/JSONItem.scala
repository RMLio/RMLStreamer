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

package io.rml.framework.flink.item.json

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.{LongNode, ObjectNode, TextNode}
import io.rml.framework.flink.item.Item
import org.jsfr.json.compiler.JsonPathCompiler
import org.jsfr.json.{JacksonParser, JsonSurfer}
import org.jsfr.json.provider.JacksonProvider
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

class JSONItem(objectNode: JsonNode) extends Item {

  val LOG = LoggerFactory.getLogger(JSONItem.getClass)

  override def refer(reference: String): Option[String] = {
    try {
      val surfer = new JsonSurfer(JacksonParser.INSTANCE, JacksonProvider.INSTANCE)
      val checkedReference = if(reference.contains('$')) reference else "$." + reference
      val iterator = surfer.iterator(objectNode.toString, JsonPathCompiler.compile(checkedReference))
      val next = iterator.next()
      require(next.isInstanceOf[TextNode] || next.isInstanceOf[LongNode], "JSONPath result is not a text node or long node: " + next.getClass)
      Some(next.toString.replaceAll("\"", ""))
    } catch {
      case NonFatal(e) => {
        e.printStackTrace()
        None
      }
    }
  }
}

object JSONItem {

  def fromString(json:String): JSONItem = {
    val mapper = new ObjectMapper()
    val node = mapper.readTree(json)
    new JSONItem(node)
  }

  def fromStringOptionable(json:String): Option[JSONItem] = {
    try {
      val mapper = new ObjectMapper()
      val node = mapper.readTree(json)
      Some(new JSONItem(node))
    } catch {
      case NonFatal(e) => e.printStackTrace(); None
    }
  }
}
