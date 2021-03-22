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

import io.rml.framework.core.item.{Item, JoinedItem}
import io.rml.framework.engine.statement.StatementEngine

abstract class StaticProcessor[T<:Item](engine: StatementEngine[T]) (implicit postProcessor: PostProcessor) extends  Processor[T, T](engine) {

  override def map(in: T): List[String] = {
    val triples = engine.process(in)

    postProcessor.process(triples)
  }
}





// Custom processing class with normal items
class StdStaticProcessor(engine: StatementEngine[Item])(implicit postProcessor: PostProcessor) extends StaticProcessor[Item](engine)


// Custom processing class with joined items
class JoinedStaticProcessor(engine: StatementEngine[JoinedItem])(implicit postProcessor: PostProcessor) extends StaticProcessor[JoinedItem](engine)

