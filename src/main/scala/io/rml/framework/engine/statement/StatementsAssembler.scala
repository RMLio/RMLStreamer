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
import io.rml.framework.core.extractors.NodeCache
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.item.{Item, JoinedItem}
import io.rml.framework.core.model._
import io.rml.framework.core.vocabulary.RDFVoc
/**
  * Creates statements from triple maps.
  */
class StatementsAssembler(subjectAssembler: SubjectGeneratorAssembler = SubjectGeneratorAssembler(),
                          predicateObjectAssembler: PredicateObjectGeneratorAssembler = PredicateObjectGeneratorAssembler(),
                          graphAssembler: GraphGeneratorAssembler = GraphGeneratorAssembler())
extends Logging{

  /**
    * Creates statements from a triple map.
    *
    * @param triplesMap
    * @return
    */
  def assembleStatements(triplesMap: TriplesMap): List[(((Item, Agent)) => Option[Iterable[TermNode]], ((Item, Agent)) => Option[Iterable[Uri]], ((Item, Agent)) => Option[Iterable[Entity]], ((Item, Agent)) => Option[Iterable[Uri]], Set[String])] = {
    this.logDebug("assembleStatements(triplesmaps)")

    val subjectLogicalTargetIds = triplesMap.subjectMap.getAllLogicalTargetIds // including GraphMap logical target IDs

    val subjectGraphGenerator = graphAssembler.assemble(triplesMap.subjectMap.graphMap, subjectLogicalTargetIds)

    // assemble subject
    val subjectGenerator = subjectAssembler.assemble(triplesMap.subjectMap, Set())  // there are no higher level logical targets since it's a "top" SubjectMap
    // check for class mappings (rr:class)
    val classMappingStatements = getClassMappingStatements(subjectGenerator, triplesMap.subjectMap.`class`, subjectGraphGenerator, subjectLogicalTargetIds)
    // assemble predicate and object
    val allPredicateObjectsAndLogicalTargetIDs = triplesMap.predicateObjectMaps.map(predicateObjectMap => {
      predicateObjectAssembler.assemble(predicateObjectMap, subjectLogicalTargetIds)
    })
    // create the statements
    val predicateObjectStatements = allPredicateObjectsAndLogicalTargetIDs.flatMap(predicateObjectAndLogicalTargetIDs => {
      val statementsPerPredicateObjectMap = predicateObjectAndLogicalTargetIDs.map(predicateObjects => {
        val graphGenerator = if(triplesMap.subjectMap.graphMap.isDefined) subjectGraphGenerator else predicateObjects._3
        (subjectGenerator, predicateObjects._1, predicateObjects._2, graphGenerator, predicateObjects._4)
      })
      statementsPerPredicateObjectMap

      /*val graphGenerator = if(triplesMap.subjectMap.graphMap.isDefined) subjectGraphGenerator else predicateObjects._3
      (subjectGenerator, predicateObjectAndLogicalTargetIDs._1, predicateObjectAndLogicalTargetIDs._2, graphGenerator, predicateObjectAndLogicalTargetIDs._4)*/
    })  // add class mappings
    predicateObjectStatements ++ classMappingStatements
  }


  private def getClassMappingStatements(subjectGenerator: ((Item, Agent)) => Option[Iterable[TermNode]],
                                        classes: List[Uri],
                                        graphGenerator: ((Item, Agent)) => Option[Iterable[Uri]],
                                        subjectLogicalTargetIds: Set[String])
  : List[(((Item, Agent)) => Option[Iterable[TermNode]], ((Item, Agent)) => Some[List[Uri]], ((Item, Agent)) => Some[List[Uri]], ((Item, Agent)) => Option[Iterable[Uri]], Set[String])] = {

    classes.map(_class => {
      val predicateGenerator = (itemAgentTuple: ((Item, Agent))) => Some(List(Uri(RDFVoc.Property.TYPE)))
      val objectGenerator = (itemAgentTuple: ((Item, Agent))) => Some(List(_class))
      (subjectGenerator, predicateGenerator, objectGenerator, graphGenerator, subjectLogicalTargetIds)
    })

  }

}

object StatementsAssembler {

  //TODO: Refactor these three methods to use pattern matching/polymorphism  (StatementType Enum might be useful)

  def assembleStatements(triplesMap: TriplesMap): List[Statement[Item]] = {
    val quads = new StatementsAssembler()
      .assembleStatements(triplesMap)
    quads.map(quad => StdStatement(quad._1, quad._2, quad._3,quad._4, quad._5))
  }

  def assembleChildStatements(joinedTriplesMap: JoinedTriplesMap): List[Statement[JoinedItem]] = {
    val triples = new StatementsAssembler()
      .assembleStatements(joinedTriplesMap)
    triples.map(triple => ChildStatement(triple._1, triple._2, triple._3, triple._4, triple._5))
  }

  def assembleParentStatements(joinedTriplesMap: JoinedTriplesMap): List[Statement[JoinedItem]] = {
    val triples = new StatementsAssembler()
      .assembleStatements(NodeCache.getTriplesMap(joinedTriplesMap.parentTriplesMap).get)
    triples.map(triple => ParentStatement(triple._1, triple._2, triple._3, triple._4, triple._5))
  }


}
