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

package io.rml.framework.engine.statement

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model._
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.flink.item.Item

/**
  *
  */
abstract class TermMapGeneratorAssembler extends Logging {

  /**
    *
    * @param termMap
    * @return
    */
  def assemble(termMap: TermMap): (Item) => Option[Iterable[Entity]] = {
    if (termMap.hasConstant) {
      constantGenerator(termMap)
    } else if (termMap.hasTemplate) {
      templateGenerator(termMap)
    } else if (termMap.hasReference) {
      referenceGenerator(termMap)
    } else if (termMap.hasTermType && termMap.termType.get == Uri(RMLVoc.Class.BLANKNODE)) {
      blankNodeGenerator()
    } else {
      if (isWarnEnabled) logWarning(termMap.toString + ": no constant, template or reference present.")
      (item: Item) => None
    }
  }

  private def blankNodeGenerator(): Item => Option[Iterable[Entity]] = {
    (item: Item) => {
      Some(List(Blank()))
    }
  }

  /**
    *
    * @param termMap
    * @return
    */
  private def constantGenerator(termMap: TermMap): Item => Option[Iterable[Entity]] = {
    termMap.termType.get.toString match {
      case RMLVoc.Class.IRI => TermMapGenerators.constantUriGenerator(termMap.constant.get)
      case RMLVoc.Class.LITERAL => TermMapGenerators.constantLiteralGenerator(termMap.constant.get, termMap.datatype, termMap.language)
    }
  }

  /**
    *
    * @param termMap
    * @return
    */
  private def templateGenerator(termMap: TermMap): Item => Option[Iterable[Entity]] = {
    termMap.termType.get.toString match {
      case RMLVoc.Class.IRI => TermMapGenerators.templateUriGenerator(termMap)
      case RMLVoc.Class.LITERAL => TermMapGenerators.templateLiteralGenerator(termMap)
      case RMLVoc.Class.BLANKNODE => TermMapGenerators.templateBlankNodeGenerator(termMap)
    }
  }

  /**
    *
    * @param termMap
    * @return
    */
  private def referenceGenerator(termMap: TermMap): Item =>Option[Iterable[Entity]] = {
    termMap.termType.get.toString match {
      case RMLVoc.Class.IRI => TermMapGenerators.referenceUriGenerator(termMap)
      case RMLVoc.Class.LITERAL => TermMapGenerators.referenceLiteralGenerator(termMap)
    }
  }

}
