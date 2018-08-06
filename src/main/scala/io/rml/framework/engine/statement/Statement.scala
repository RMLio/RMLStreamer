/*
 * Copyright (c) 2017 Ghent University - imec
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.rml.framework.engine.statement

import io.rml.framework.core.model._
import io.rml.framework.flink.item.{Item, JoinedItem}
import io.rml.framework.flink.sink._

/**
  * Represents a potential triple. A statement potentially generates a triple
  * from a given item by using the injected generators.
  *
  * This class is used by the StatementEngine implementation.
  */

abstract class Statement[T] {

  val subjectGenerator: Item => Option[Iterable[TermNode]]
  val predicateGenerator: Item => Option[Iterable[Uri]]
  val objectGenerator: Item => Option[Iterable[Entity]]
  val graphGenerator:  Item => Option[Iterable[Uri]]



  def process(item: T): Option[Iterable[FlinkRDFQuad]]


  def subProcess[S <: Item] (graphItem:S, subjItem:S, predItem: S, objectItem: S): Option[Iterable[FlinkRDFQuad]] = {
    val graphOption = graphGenerator(graphItem)

    val result = for {
      subject <- subjectGenerator(subjItem) // try to generate the subject
      predicate <- predicateGenerator(predItem) // try to generate the  predicate
      _object <- objectGenerator(objectItem ) // try to generate the object
    } yield for {
      (subj, pred, obj, graph) <- Statement.quadCombination(subject, predicate, _object, graphOption)
      triple <- Statement.generateQuad(subj, pred, obj, graph)

    } yield triple


    if (result.isEmpty) None else result
  }
}


case class ChildStatement(subjectGenerator: Item => Option[Iterable[TermNode]],
                     predicateGenerator: Item => Option[Iterable[Uri]],
                     objectGenerator: Item => Option[Iterable[Entity]],
                     graphGenerator: Item => Option[Iterable[Uri]]) extends Statement[JoinedItem] with Serializable {

  def process(item: JoinedItem): Option[Iterable[FlinkRDFQuad]] = {
    subProcess(item.child, item.child, item.child, item.parent)
  }
}

case class ParentStatement(subjectGenerator: Item => Option[Iterable[TermNode]],
                      predicateGenerator: Item => Option[Iterable[Uri]],
                      objectGenerator: Item => Option[Iterable[Entity]],
                      graphGenerator: Item => Option[Iterable[Uri]]) extends Statement[JoinedItem] with Serializable {

  def process(item: JoinedItem): Option[Iterable[FlinkRDFQuad]] = {
    subProcess(item.parent, item.parent, item.parent, item.parent)
  }
}

case class StdStatement(subjectGenerator: Item => Option[Iterable[TermNode]],
                   predicateGenerator: Item => Option[Iterable[Uri]],
                   objectGenerator: Item => Option[Iterable[Entity]],
                   graphGenerator: Item => Option[Iterable[Uri]]) extends Statement[Item] with Serializable {

  /**
    * Tries to refer a triple from the given item.
    *
    * @param item
    * @return
    */
  def process(item: Item): Option[Iterable[FlinkRDFQuad]] = {

    subProcess(item,item,item,item)
  }

}

object Statement {

  def quadCombination(subjectIter: Iterable[TermNode], predicateIter: Iterable[Uri], objIter: Iterable[Entity], graphIterOpt: Option[Iterable[Uri]] = None): Iterable[(TermNode, Uri, Entity, Option[Uri])] = {

    val graphIter: Iterable[Uri] = graphIterOpt getOrElse List()

    //TODO: Smells bad, might have to refactor this
    val triple = for {
      subj <- subjectIter
      pred <- predicateIter
      obj <- objIter
    } yield (subj, pred, obj, None)

    if (graphIter.nonEmpty) {
      for {
        graph <- graphIter
        (subj, pred, obj, _) <- triple
      } yield (subj, pred, obj, Some(graph))

    } else {
      triple
    }
  }


  def generateQuad(subject: TermNode, predicate: Uri, _object: Entity, graphOpt: Option[Uri] = None): Option[FlinkRDFQuad] = {

    val subjectResource = subject match {
      case blank: Blank => FlinkRDFBlank(blank)
      case resource: Uri => FlinkRDFResource(resource)
    }
    val predicateResource = FlinkRDFResource(predicate)
    val objectNode = _object match {
      case literal: Literal => FlinkRDFLiteral(literal)
      case resource: Uri => FlinkRDFResource(resource)
      case blank: Blank => FlinkRDFBlank(blank)
    }
    val graphUri = graphOpt.map(FlinkRDFResource)

    val result = Some(FlinkRDFQuad(subjectResource, predicateResource, objectNode, graphUri))
    println(result)
    result
  }
}
