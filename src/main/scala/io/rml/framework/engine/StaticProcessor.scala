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

import be.ugent.idlab.knows.functions.agent.Agent
import io.rml.framework.core.item.{Item, JoinedItem}
import io.rml.framework.engine.statement.StatementEngine

abstract class StaticProcessor[T<:Item, A <: Agent](engine: StatementEngine[T, A]) (implicit postProcessor: PostProcessor) extends  Processor[T, T, A](engine) {

  /**
    * Maps items to a iterable of (logical target ID, rendered RDF statement(s)) pairs
    * @param in Input items, in RMLStreamer of [[Item]] type
    * @return
    */
  override def map(in: T): Iterable[(String, String)] = {
    val triples = engine.process(in, functionAgent.get)

    postProcessor.process(triples)
  }
}





// Custom processing class with normal items
class StdStaticProcessor(engine: StatementEngine[Item, Agent])(implicit postProcessor: PostProcessor) extends StaticProcessor[Item, Agent](engine)


// Custom processing class with joined items
class JoinedStaticProcessor(engine: StatementEngine[JoinedItem, Agent])(implicit postProcessor: PostProcessor) extends StaticProcessor[JoinedItem, Agent](engine)

