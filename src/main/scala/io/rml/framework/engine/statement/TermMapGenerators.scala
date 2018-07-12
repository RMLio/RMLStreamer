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

import io.rml.framework.core.model._
import io.rml.framework.engine.Engine
import io.rml.framework.flink.item.Item

/**
  *
  */
object TermMapGenerators {

  def constantUriGenerator(constant: Entity): Item => Option[Uri] = {
    // return a function that just returns the constant
    (item: Item) => {
      Some(Uri(constant.toString))
    }
  }

  def constantLiteralGenerator(constant: Entity, datatype: Option[Uri] = None, language: Option[Literal]): Item => Option[Literal] = {
    // return a function that just returns the constant
    (item: Item) => {
      Some(Literal(constant.toString, datatype, language))
    }

  }

  def templateUriGenerator(termMap: TermMap): Item => Option[Uri] = {
    // return a function that processes the template
    (item: Item) => {
      for {
        value <- Engine.processTemplate(termMap.template.get, item, encode = true)
        uri <- Some(Uri(value))
      } yield uri
    }
  }

  def templateLiteralGenerator(termMap: TermMap): Item => Option[Literal] = {
    // return a function that processes the template
    (item: Item) => {
      for {
        value <- Engine.processTemplate(termMap.template.get, item)
        uri <- Some(Literal(value, termMap.datatype, termMap.language))
      } yield uri
    }
  }

  def templateBlankNodeGenerator(termMap: TermMap): Item => Option[Blank] = {
    (item: Item) => {
      for {
        value <- Engine.processTemplate(termMap.template.get, item, encode = true)
        blank <- Some(Blank(value))
      } yield blank
    }
  }

  def referenceLiteralGenerator(termMap: TermMap): Item => Option[Literal] = {
    // return a function that processes a reference
    (item: Item) => {
      for {
        value <- Engine.processReference(termMap.reference.get, item)
        uri <- Some(Literal(value, termMap.datatype, termMap.language))
      } yield uri
    }
  }

  def referenceUriGenerator(termMap: TermMap): Item => Option[Uri] = {
    // return a function that processes a reference
    (item: Item) => {
      for {
        value <- Engine.processReference(termMap.reference.get, item, encode = true)
        uri <- Some(Uri(value))
      } yield uri
    }
  }

}
