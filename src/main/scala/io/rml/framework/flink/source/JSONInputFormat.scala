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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.rml.framework.core.item.Item
import io.rml.framework.core.item.json.JSONItem
import io.rml.framework.core.util.Util.DEFAULT_ITERATOR_MAP
import io.rml.framework.core.vocabulary.RMLVoc
import org.apache.flink.api.common.io.{GenericInputFormat, NonParallelInput}
import org.apache.flink.core.io.GenericInputSplit
import org.jsfr.json.compiler.JsonPathCompiler
import org.jsfr.json.provider.JacksonProvider
import org.jsfr.json.{JacksonParser, JsonSurfer}

import java.io.{FileInputStream, InputStream}
import java.util

class JSONInputFormat(path: String, jsonPath: String) extends GenericInputFormat[Item] with NonParallelInput {

  private var iterator: util.Iterator[Object] = _
  private var inputStream: InputStream = _
  private val DEFAULT_PATH_OPTION: String = DEFAULT_ITERATOR_MAP(RMLVoc.Class.JSONPATH)

  override def open(inputSplit: GenericInputSplit): Unit = {
    super.open(inputSplit)
    val surfer = new JsonSurfer(JacksonParser.INSTANCE, JacksonProvider.INSTANCE)
    inputStream = new FileInputStream(path)
    iterator = surfer.iterator(inputStream, JsonPathCompiler.compile(jsonPath))

  }

  override def reachedEnd(): Boolean = !iterator.hasNext

  override def nextRecord(reuse: Item): JSONItem = {
    val _object = iterator.next()
    val asInstanceOf = _object.asInstanceOf[ObjectNode]
    val mapper = new ObjectMapper()
    val map = mapper.convertValue(asInstanceOf, classOf[java.util.Map[String, Object]])
    val tag = jsonPath match {
      case DEFAULT_PATH_OPTION => ""
      case _ => jsonPath
    }
    new JSONItem(map, tag)
  }

  override def close(): Unit = {
    inputStream.close()
    super.close()
  }
}
