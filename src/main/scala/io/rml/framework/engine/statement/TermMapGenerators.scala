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
 * */

package io.rml.framework.engine.statement


import be.ugent.idlab.knows.functions.agent.Agent
import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.function.FunctionUtils
import io.rml.framework.core.item.Item
import io.rml.framework.core.model._
import io.rml.framework.core.util.Util
import io.rml.framework.engine.Engine

/**
 *
 */
object TermMapGenerators {

  def constantUriGenerator(constant: Entity): ((Item, Agent)) => Option[Iterable[Uri]] = {
    // return a function that just returns the constant
    itemAgentTuple => {
      Some(List(Uri(constant.value)))
    }
  }

  def constantLiteralGenerator(constant: Entity, datatype: Option[Uri] = None, language: Option[Literal]): ((Item, Agent)) => Option[Iterable[Literal]] = {
    // return a function that just returns the constant
    itemAgentTuple => {
      Some(List(Literal(constant.value, datatype, language)))
    }

  }

  def templateUriGenerator(termMap: TermMap): ((Item, Agent)) => Option[Iterable[Uri]] = {
    // return a function that processes the template
    itemAgentTuple => {

      for {
        iter <- Engine.processTemplate(termMap.template.get, itemAgentTuple._1, encode = true)
      } yield for {
        value <- iter
        processed <- processIRI(value)
        uri = Uri(processed)
      } yield uri
    }
  }

  def templateLiteralGenerator(termMap: TermMap): ((Item, Agent)) => Option[Iterable[Literal]] = {
    // return a function that processes the template
    itemAgentTuple => {
      for {
        iter <- Engine.processTemplate(termMap.template.get, itemAgentTuple._1)
      } yield for {
        value <- iter
        lit = Literal(value, termMap.datatype, termMap.language)

      } yield lit
    }
  }

  def templateBlankNodeGenerator(termMap: TermMap): ((Item, Agent)) => Option[Iterable[Blank]] = {
    itemAgentTuple => {

      for {
        iter <- Engine.processTemplate(termMap.template.get, itemAgentTuple._1, encode = true)
      } yield for {
        value <- iter
        blank = Blank(value)
      } yield blank
    }
  }

  def referenceLiteralGenerator(termMap: TermMap): ((Item, Agent)) => Option[Iterable[Literal]] = {
    // return a function that processes a reference
    itemAgentTuple => {
      for {
        iter <- Engine.processReference(termMap.reference.get, itemAgentTuple._1)
      } yield for {
        value <- iter
        item = itemAgentTuple._1
        reference = termMap.reference.get.value
        potentialType = item.getDataTypes.get(reference)

        lit =
          if (potentialType.isDefined) {
            val `type` = Uri(potentialType.get)
            // cast the value to correct notation
            // little hack: wrap the value in Uri to allow usage in the typecasting code
            val l = FunctionUtils.typeCastDataType(Uri(value), Some(`type`))
            Literal(l.get.value, Some(`type`), termMap.language)
          }
          else
            Literal(value, termMap.datatype, termMap.language)

      } yield lit
    }
  }

  def referenceUriGenerator(termMap: TermMap): ((Item, Agent)) => Option[Iterable[Uri]] = {
    // return a function that processes a reference
    itenAgentTuple => {
      for {
        iter <- Engine.processReference(termMap.reference.get, itenAgentTuple._1)

      } yield for {
        iri <- iter
        processed <- processIRI(iri)
        uri = Uri(processed)
      } yield uri
    }
  }

  private def processIRI(origIri: String): Iterable[String] = {
    if (Util.isValidAbsoluteUri(origIri)) {
      List(origIri)
    } else {
      val baseIRI = RMLEnvironment.getGeneratorBaseIRI()
      if (baseIRI.isDefined) {
        val completeUri = baseIRI.get + origIri
        if (Util.isValidAbsoluteUri(completeUri)) {
          List(completeUri)
        } else {
          List()
        }
      } else {
        List()
      }
    }
  }
}
