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

import io.rml.framework.core.internal.Logging
import io.rml.framework.core.item.{Item, JoinedItem}
import io.rml.framework.core.model.rdf.SerializableRDFQuad
import io.rml.framework.core.model.{JoinedTriplesMap, TriplesMap}
import io.rml.framework.core.util.Util.DEFAULT_ITERATOR_SET
import io.rml.framework.engine.Engine

/**
  * A statement engine is an engine implementation that makes use of a transformed
  * RML mapping document. The transformation generates a set of statements that
  * each represent a triple that should be generated. The represented triple still misses
  * the data needed from an item (e.g. through referencing). The engine iterates over the
  * list of transformed statements for each item. The transformation occurs at
  * the start after loading the RML mapping document. This way, the engine avoids
  * iterating over the RML mapping model objects each time an item is being referred and
  * only needs to iterate over the prepared statements (which will be maximally equal in amount).
  *
  * @param statementMap
  */
class StatementEngine[T <: Item](val statementMap: Map[String, List[Statement[T]]]) extends Engine[T] {


  /**
    * Process an item.
    *
    * @param item
    * @return
    */
  override def process(item: T): List[SerializableRDFQuad] = {

    val statements = statementMap.getOrElse(item.tag, List())

    statements.flatMap(statement => {
      statement.process(item)
    })
    .flatten
  }

}

/**
  *
  */
object StatementEngine extends Logging {

  /**
    *
    * @param triplesMaps
    * @return
    */
  def fromTriplesMaps(triplesMaps: List[TriplesMap]): StatementEngine[Item] = {
    // assemble the statements
    this.logDebug("fromTriplesMaps(triplesMaps) - creating statement engine")
    //Group the triple maps with their iterator as the key
    val iteratorGroup = triplesMaps.groupBy(tm => {
      // here we require the triples map to have only one iterator
      val iteratorQuery = tm.logicalSource.iterators.head
      if (DEFAULT_ITERATOR_SET.contains(iteratorQuery)) {
        ""
      } else {
        iteratorQuery
      }

      //tm.logicalSource.iterators.head
    })

    // Transform key from Option[Literal] -> Option[String]
    val groupedStatements: Map[String, List[Statement[Item]]] =
      iteratorGroup
      .mapValues(tms => tms.flatMap(StatementsAssembler.assembleStatements))
      .map(identity)  // to avoid this bug: https://github.com/scala/bug/issues/7005

    // do some logging
    if (isDebugEnabled) logDebug(groupedStatements.values.flatten.size + " statements were generated.")

    // create the engine instance
    new StatementEngine(groupedStatements)
  }

  /**
    * Assumes that the triple map only contains predicate object maps with the same PTM and join conditions (JoinedTripleMap).
    *
    * @param triplesMap
    * @return
    */
  def fromJoinedTriplesMap(triplesMap: JoinedTriplesMap): StatementEngine[JoinedItem] = {
    val childStatements = StatementsAssembler.assembleChildStatements(triplesMap)
    val parentStatements = StatementsAssembler.assembleParentStatements(triplesMap)
    // do some logging
    if (isDebugEnabled) logDebug((childStatements.size + parentStatements.size) + " statements were generated.")
    new StatementEngine(Map("" -> {
      childStatements ++ parentStatements
    }))
  }

}
