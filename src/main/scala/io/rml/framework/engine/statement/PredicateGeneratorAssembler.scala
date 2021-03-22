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

import io.rml.framework.core.function.FunctionUtils
import io.rml.framework.core.item.Item
import io.rml.framework.core.model.{TermMap, Uri}

class PredicateGeneratorAssembler extends TermMapGeneratorAssembler {

  override def assemble(termMap: TermMap): (Item) => Option[Iterable[Uri]] = {

    // Note: this code is very redundant to ObjectGeneratorAssembler. TODO: generalize?
    if(termMap.hasFunctionMap){
      val fmap = termMap.functionMap.head
      val assembledFunction = FunctionMapGeneratorAssembler().assemble(fmap)
      assembledFunction.andThen(item => {
        if(item.isDefined) {
          item.map(iter => iter.flatMap(elem => {
            val castedResult = FunctionUtils.typeCastDataType(elem, termMap.datatype)
            castedResult.map(v => Uri(v.value))
          }))
        }else {
          None
        }
      })
    }
    else {
      super.assemble(termMap).asInstanceOf[(Item) => Option[Iterable[Uri]]]
    }
  }

}

object PredicateGeneratorAssembler {
  def apply(): PredicateGeneratorAssembler = new PredicateGeneratorAssembler()
}

