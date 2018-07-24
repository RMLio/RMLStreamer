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
import io.rml.framework.flink.sink.{FlinkRDFBlank, FlinkRDFLiteral, FlinkRDFResource, FlinkRDFTriple}

/**
  * Represents a potential triple. A statement potentially generates a triple
  * from a given item by using the injected generators.
  *
  * This class is used by the StatementEngine implementation.
  */

trait Statement[T] {
  def process(item: T): Option[Iterable[FlinkRDFTriple]]
}

class ChildStatement(subjectGenerator: Item => Option[Iterable[TermNode]],
                     predicateGenerator: Item => Option[Iterable[Uri]],
                     objectGenerator: Item => Option[Iterable[Entity]]) extends Statement[JoinedItem] with Serializable {

  def process(item: JoinedItem): Option[Iterable[FlinkRDFTriple]] = {
    val result = for {
      subject <- subjectGenerator(item.child) // try to generate the subject
      predicate <- predicateGenerator(item.child) // try to generate the  predicate
      _object <- objectGenerator(item.parent) // try to generate the object
    } yield for{
      (sub, pred, obj) <- Statement.tripleCombination(subject, predicate, _object)
      triple <- {
        println("TRIPLE PROV")
        println(item)
        Statement.generateTriple(sub, pred, obj)
      } // generate the triple
    }yield triple // this can be Some[RDFTriple] or None

    if (result.isEmpty) None else result

  }
}

class ParentStatement(subjectGenerator: Item => Option[Iterable[TermNode]],
                      predicateGenerator: Item => Option[Iterable[Uri]],
                      objectGenerator: Item => Option[Iterable[Entity]]) extends Statement[JoinedItem] with Serializable {

  def process(item: JoinedItem): Option[Iterable[FlinkRDFTriple]] = {
    val result = for {
      subject <- subjectGenerator(item.parent) // try to generate the subject
      predicate <- predicateGenerator(item.parent) // try to generate the  predicate
      _object <- objectGenerator(item.parent) // try to generate the object
    } yield for{
      (sub, pred, obj) <- Statement.tripleCombination(subject, predicate, _object)
      triple <- {
        println("TRIPLE PROV")
        println(item)
        Statement.generateTriple(sub, pred, obj)
      } // generate the triple
    }yield triple // this can be Some[RDFTriple] or None

    if (result.isEmpty) None else result
  }
}

class StdStatement(subjectGenerator: Item => Option[Iterable[TermNode]],
                   predicateGenerator: Item => Option[Iterable[Uri]],
                   objectGenerator: Item => Option[Iterable[Entity]]) extends Statement[Item] with Serializable {

  /**
    * Tries to refer a triple from the given item.
    *
    * @param item
    * @return
    */
  def process(item: Item): Option[Iterable[FlinkRDFTriple]] = {
    val result =
      for {
        subject <- subjectGenerator(item) // try to generate the subject
        predicate <- predicateGenerator(item) // try to generate the  predicate
        _object <- objectGenerator(item) // try to generate the object
      } yield for{
        (sub, pred, obj) <- Statement.tripleCombination(subject, predicate, _object)
        triple <- {
          println("TRIPLE PROV")
          println(item)
          Statement.generateTriple(sub, pred, obj)
        } // generate the triple
      }yield triple // this can be Some[RDFTriple] or None

    if (result.isEmpty) None else result
  }

}

object Statement {

  def createStandardStatement(subject: Item => Option[Iterable[TermNode]],
                              predicate: Item => Option[Iterable[Uri]],
                              `object`: Item => Option[Iterable[Entity]])

  : Statement[Item] = new StdStatement(subject: Item => Option[Iterable[TermNode]],
    predicate: Item => Option[Iterable[Uri]],
    `object`: Item => Option[Iterable[Entity]])

  def createChildStatement(subject: Item => Option[Iterable[TermNode]],
                           predicate: Item => Option[Iterable[Uri]],
                           `object`: Item => Option[Iterable[Entity]])
  : Statement[JoinedItem] = new ChildStatement(subject, predicate, `object`)

  def createParentStatement(subject: Item => Option[Iterable[TermNode]],
                            predicate: Item => Option[Iterable[Uri]],
                            `object`: Item => Option[Iterable[Entity]])
  : Statement[JoinedItem] = new ParentStatement(subject, predicate, `object`)

  def tripleCombination(subjectIter: Iterable[TermNode], predicateIter: Iterable[Uri], objIter: Iterable[Entity]): Iterable[(TermNode, Uri, Entity)] = {
    for {
      subj <- subjectIter
      pred <- predicateIter
      obj <- objIter
    } yield (subj, pred, obj)
  }

  def generateTriple(subject: TermNode, predicate: Uri, _object: Entity): Option[FlinkRDFTriple] = {

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
    println(Some(FlinkRDFTriple(subjectResource, predicateResource, objectNode)).get.toString)
    Some(FlinkRDFTriple(subjectResource, predicateResource, objectNode))

  }
}
