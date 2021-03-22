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

import io.rml.framework.core.item.Item
import io.rml.framework.engine.statement.StatementEngine
import org.apache.flink.api.common.functions.RichMapFunction

/**
  * Abstract class for creating a custom processing mapping step in a pipeline.
  * Extends a RichFunction to have access to the RuntimeContext.
  *
  * Takes [[Item]]
  * @param engine statement engine which will be used to process items
  * @param postProcessor post processor to process the generated triples from the items
  * @tparam T has upper bound of [[Item]]
  * @tparam IN specifies the type of the input for the map(..) function
  */
abstract class Processor[T<:Item, IN](engine: StatementEngine[T])(implicit postProcessor: PostProcessor) extends RichMapFunction[IN, List[String]]

