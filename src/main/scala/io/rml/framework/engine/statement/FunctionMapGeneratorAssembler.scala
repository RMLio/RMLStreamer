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

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.model._
import io.rml.framework.core.vocabulary.RMLVoc
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.sink.FlinkRDFQuad

case class FunctionMapGeneratorAssembler() extends TermMapGeneratorAssembler {

  override def assemble(termMap: TermMap): (Item) => Option[Iterable[Entity]] = {
    require(termMap.isInstanceOf[FunctionMap], "Wrong TermMap instance.")

    val functionMap = termMap.asInstanceOf[FunctionMap]
    val functionEngine = StatementEngine.fromTriplesMaps(List(functionMap.functionValue))
    (item: Item) => {
      val triples: List[FlinkRDFQuad] = functionEngine.process(item)
      val parameters: Map[Uri, String] = triples.filter(triple => triple.predicate.uri != Uri(RMLVoc.Property.EXECUTES))
        .map(triple => {
          val parameterName = triple.predicate.uri
          val parameterValue = triple.`object`.value.toString
          parameterName -> parameterValue
        })
        .toMap

      val name: Uri = Uri(triples.filter(triple => triple.predicate.uri == Uri(RMLVoc.Property.EXECUTES))
        .head.`object`.value
        .toString)

      require(RMLEnvironment.hasTransformationRegistered(name), "Transformation " + name + " is not registered.")
      RMLEnvironment.getTransformation(name).get.execute(Parameters(parameters))

      //TODO: PLACEHOLDER REMOVE THIS WHEN FUNCTION MAP IS IMPLEMENTED !!!!!
      None  // <---- REMOVE THIS!!!!!!!!!!!!
    }
  }

}
