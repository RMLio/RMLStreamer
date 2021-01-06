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
package io.rml.framework.api

import io.rml.framework.core.model.Uri
import io.rml.framework.flink.item.Item

import scala.collection.mutable.{Map => MutableMap}

object RMLEnvironment {


  private val sources: MutableMap[Uri, Iterable[Item]] = MutableMap()
  private var generatorBaseIRI: Option[String] = None
  private var mappingFileBaseIRI: Option[String] = None

  def setGeneratorBaseIRI(baseIRI: Option[String]) = {
    generatorBaseIRI = baseIRI
  }

  def getGeneratorBaseIRI(): Option[String] = {
    generatorBaseIRI
  }

  def setMappingFileBaseIRI(baseIRI: Option[String]) = {
    mappingFileBaseIRI = baseIRI
  }

  def getMappingFileBaseIRI(): Option[String] = {
    mappingFileBaseIRI
  }
  
  def executeMapping(): Unit = ???

  def executeTriplesMap(): Unit = ???


  def registerSource(uri: Uri, iterable: Iterable[Item]): Unit = {
    require(sources.isEmpty, "Processing of only one source supported in API mode.")
    sources.put(uri, iterable)
  }


  def getSource(uri: Uri): Option[Iterable[Item]] = {
    sources.get(uri)
  }
  
  def reset(): Unit = {
    sources.clear()
  }

}
