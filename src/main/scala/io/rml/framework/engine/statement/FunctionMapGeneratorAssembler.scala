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

import be.ugent.idlab.knows.functions.agent.{Agent, Arguments}
import io.rml.framework.core.function.model.Function
import io.rml.framework.core.item.{EmptyItem, Item}
import io.rml.framework.core.model._
import io.rml.framework.core.model.rdf.SerializableRDFQuad
import io.rml.framework.core.vocabulary.FunVoc
import io.rml.framework.shared.RMLException

case class FunctionMapGeneratorAssembler() extends TermMapGeneratorAssembler {


  override def assemble(termMap: TermMap, higherLevelLogicalTargetIDs: Set[String]): ((Item, Agent)) => Option[Iterable[Entity]] = {
    require(termMap.isInstanceOf[FunctionMap], "Wrong TermMap instance.")

    val functionMap = termMap.asInstanceOf[FunctionMap]
    val pomAssembler = PredicateObjectGeneratorAssembler()

    val assembledPom = functionMap.functionValue.sortBy(_.identifier) // sortBy required for retaining correct parameter ordering
      .flatMap(predicateObjectMap => pomAssembler.assemble(predicateObjectMap, higherLevelLogicalTargetIDs))
      .map {
        case (predicateGen, objGen, _, logicalTargetIDs) => (predicateGen, objGen, logicalTargetIDs)
      }

    parseFunction(assembledPom)
  }

  private def parseFunction(assembledPom:
                            List[(((Item, Agent)) => Option[Iterable[Uri]], ((Item, Agent)) => Option[Iterable[Entity]], Set[String])]): ((Item, Agent)) => Option[Iterable[Entity]]  = {

    this.logDebug("parseFunction (assembledPom)")
    val placeHolder: List[SerializableRDFQuad] = generateFunctionTriples(((new EmptyItem(), new Agent {
      override def execute(functionId: String, arguments: Arguments): AnyRef = ???
    })), assembledPom)

    val executeProperties = placeHolder.filter( quad => quad.predicate.value == Uri(FunVoc.FnO.Property.EXECUTES))
    if(executeProperties.isEmpty)
      throw new RMLException(s"Couldn't find ${FunVoc.FnO.Property.EXECUTES} property." +
        s"Is the namespace correct? (e.g. HTTP vs. HTTPS)")

    val functionName = Uri(
      executeProperties
      .head
      .`object`
      .value
      .value)

    createAssemblerFunction(functionName, assembledPom)
  }

  /**
   * Generates an assembler function which takes in [[Item]] and generate
   * entities using the function specified by the function map
   *
   * @param assembledPom List of predicate object generator functions
   * @return anon function taking in [[Item]] and returns entities using the function
   */
  private def createAssemblerFunction(functionName: Uri, assembledPom: List[(((Item, Agent)) => Option[Iterable[Uri]], ((Item, Agent)) => Option[Iterable[Entity]], Set[String])]): ((Item, Agent)) => Option[Iterable[Entity]] = {
    itemAgentTuple => {
      val triples: List[SerializableRDFQuad] = generateFunctionTriples(itemAgentTuple, assembledPom)
      val paramTriples = triples.filter(triple => triple.predicate.uri != Uri(FunVoc.FnO.Property.EXECUTES))

      Function.execute(functionName.identifier, paramTriples, itemAgentTuple._2)
    }
  }

  /**
   * Generate triples from which the the function can be derived from
   * and applied to the the item
   *
   * @param item
   * @return
   */
  private def generateFunctionTriples(itemAgentTuple: ((Item, Agent)), assembledPom: List[(((Item, Agent)) => Option[Iterable[Uri]], ((Item, Agent)) => Option[Iterable[Entity]], Set[String])]): List[SerializableRDFQuad] = {

    val result = for{
      (predicateGen, objGen, logicalTargetIDs) <- assembledPom
      predicateIter <- predicateGen(itemAgentTuple)
      objIter <- objGen(itemAgentTuple)
    } yield for {
      predicate <- predicateIter
      obj <- objIter
      quad <-  Statement.generateQuad(Blank(), predicate, obj, logicalTargetIDs)
    } yield quad


    result.flatten

  }

}
