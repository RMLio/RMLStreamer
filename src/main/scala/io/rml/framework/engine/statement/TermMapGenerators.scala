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

package io.rml.framework.engine.statement


import java.net.{URI, URISyntaxException, URL}

import io.rml.framework.core.model._
import io.rml.framework.engine.Engine
import io.rml.framework.flink.item.Item
import org.apache.commons.validator.routines.UrlValidator
import org.apache.jena.riot.system.IRIResolver

/**
  *
  */
object TermMapGenerators {

  private var BASE_URL = ""
  private var BASE_IS_SET = false
  private val VALIDATOR = new UrlValidator()

  def setBaseURL(url: String): Unit = {
    if (BASE_URL.isEmpty & !BASE_IS_SET) {
      BASE_URL = url
      BASE_IS_SET = true
    }

  }

  def constantUriGenerator(constant: Entity): Item => Option[Iterable[Uri]] = {
    // return a function that just returns the constant
    (item: Item) => {
      Some(List(Uri(constant.toString)))
    }
  }

  def constantLiteralGenerator(constant: Entity, datatype: Option[Uri] = None, language: Option[Literal]): Item => Option[Iterable[Literal]] = {
    // return a function that just returns the constant
    (item: Item) => {
      Some(List(Literal(constant.toString, datatype, language)))
    }

  }

  def templateUriGenerator(termMap: TermMap): Item => Option[Iterable[Uri]] = {
    // return a function that processes the template
    (item: Item) => {

      for {
        iter <- Engine.processTemplate(termMap.template.get, item, encode = true)
      } yield for {
        value <- iter
        uri = Uri(value)
      } yield uri
    }
  }

  def templateLiteralGenerator(termMap: TermMap): Item => Option[Iterable[Literal]] = {
    // return a function that processes the template
    (item: Item) => {
      for {
        iter <- Engine.processTemplate(termMap.template.get, item)
      } yield for {
        value <- iter
        lit = Literal(value, termMap.datatype, termMap.language)

      } yield lit
    }
  }

  def templateBlankNodeGenerator(termMap: TermMap): Item => Option[Iterable[Blank]] = {
    (item: Item) => {

      for {
        iter <- Engine.processTemplate(termMap.template.get, item, encode = true)
      } yield for {
        value <- iter
        blank = Blank(value)
      } yield blank
    }
  }

  def referenceLiteralGenerator(termMap: TermMap): Item => Option[Iterable[Literal]] = {
    // return a function that processes a reference
    (item: Item) => {
      for {
        iter <- Engine.processReference(termMap.reference.get, item)

      } yield for {
        value <- iter
        lit = Literal(value, termMap.datatype, termMap.language)

      } yield lit
    }
  }

  def referenceUriGenerator(termMap: TermMap): Item => Option[Iterable[Uri]] = {
    // return a function that processes a reference
    (item: Item) => {
      for {
        iter <- Engine.processReference(termMap.reference.get, item)

      } yield for {
        iri <- iter
        processed <- processIRI(iri)
        uri = Uri(processed)
      } yield uri
    }
  }

  def processIRI(origIRI: String): Iterable[String] = {
    /**
      * Extra check to prevent calling validator which costs more time if
      * done repeatedly per item since it uses regex
      */
    val default = origIRI
    if (BASE_IS_SET) {

      val appended = BASE_URL + origIRI

      if (VALIDATOR.isValid(appended)) {

        List(appended)

      } else if (VALIDATOR.isValid(default)) {
      cd
        List(default)

      } else {

        List()

      }


    } else {
      List(default)
    }


  }


}
