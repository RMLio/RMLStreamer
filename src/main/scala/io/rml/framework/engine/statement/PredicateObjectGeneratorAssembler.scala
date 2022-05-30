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

import be.ugent.idlab.knows.functions.agent.Agent
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.item.Item
import io.rml.framework.core.model.{Entity, PredicateObjectMap, Uri}
class PredicateObjectGeneratorAssembler(predicateGeneratorAssembler: PredicateGeneratorAssembler,
                                        objectGeneratorAssembler: ObjectGeneratorAssembler,
                                        graphGeneratorAssembler: GraphGeneratorAssembler) extends Logging{

  def assemble(predicateObjectMap: PredicateObjectMap, higherLevelLogicalTargetIDs: Set[String])
  : List[(((Item, Agent)) => Option[Iterable[Uri]], ((Item, Agent)) => Option[Iterable[Entity]], ((Item, Agent)) => Option[Iterable[Uri]], Set[String])] = {

    this.logDebug("assemble (predicateObjectMap)")
    val graphLogicalTargetIDs = if (predicateObjectMap.graphMap.isDefined) {
      predicateObjectMap.graphMap.get.getAllLogicalTargetIds
    } else {
      Set()
    }

    val graphStatement = graphGeneratorAssembler.assemble(predicateObjectMap.graphMap, higherLevelLogicalTargetIDs ++ graphLogicalTargetIDs)
    predicateObjectMap.predicateMaps.flatMap(predicateMap => {
      val predicateLogicalTargetIDs = predicateMap.getAllLogicalTargetIds
      predicateObjectMap.objectMaps.map(objectMap => {
        val objectLogicalTargetIDs = objectMap.getAllLogicalTargetIds
        val allLogicalTargetIDs = higherLevelLogicalTargetIDs ++ predicateLogicalTargetIDs ++ objectLogicalTargetIDs ++ graphLogicalTargetIDs
        (predicateGeneratorAssembler.assemble(predicateMap, allLogicalTargetIDs),
          objectGeneratorAssembler.assemble(objectMap, allLogicalTargetIDs),
          graphStatement,
          allLogicalTargetIDs)
      })

    })

  }
}

object PredicateObjectGeneratorAssembler {

  def apply(
             predicateGeneratorAssembler: PredicateGeneratorAssembler = PredicateGeneratorAssembler(),
             objectGeneratorAssembler: ObjectGeneratorAssembler = ObjectGeneratorAssembler(),
             graphGeneratorAssembler: GraphGeneratorAssembler = GraphGeneratorAssembler())

  : PredicateObjectGeneratorAssembler = new PredicateObjectGeneratorAssembler(predicateGeneratorAssembler,
    objectGeneratorAssembler, graphGeneratorAssembler)
}
