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

import io.rml.framework.core.model.{GraphMap, TermMap, Uri}
import io.rml.framework.core.util.Util
import io.rml.framework.flink.item.Item

class GraphGeneratorAssembler extends TermMapGeneratorAssembler {

  def assemble(graphMapOpt: Option[GraphMap]): Item => Option[Iterable[Uri]] = {
    graphMapOpt match {
      case None => Item => None
      case Some(map) => assemble(map)
    }
  }


  override def assemble(termMap: TermMap): Item => Option[Iterable[Uri]] =
    if (termMap.hasFunctionMap) {
      val assembled = FunctionMapGeneratorAssembler().assemble(termMap.functionMap.head)


      assembled.andThen(result => {
        result.map(iter => iter.map(elem => Uri(elem.toString)))
      })
    } else {
    super.assemble(termMap).asInstanceOf[(Item) => Option[Iterable[Uri]]]
    }


}

object GraphGeneratorAssembler {
  def apply(): GraphGeneratorAssembler = new GraphGeneratorAssembler()
}
