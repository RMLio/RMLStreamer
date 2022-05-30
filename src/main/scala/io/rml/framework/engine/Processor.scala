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
import io.rml.framework.core.item.Item
import io.rml.framework.engine.statement.StatementEngine
import io.rml.framework.flink.util.FunctionsFlinkUtil
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration

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
abstract class Processor[T<:Item, IN, A <: Agent](engine: StatementEngine[T, A])(implicit postProcessor: PostProcessor) extends RichMapFunction[IN, Iterable[(String, String)]] {
  protected var functionAgent: Option[Agent] = None

  override def open(parameters: Configuration): Unit = {
    functionAgent = Some(FunctionsFlinkUtil.initFunctionAgentForProcessor(getRuntimeContext.getDistributedCache));
  }
}
