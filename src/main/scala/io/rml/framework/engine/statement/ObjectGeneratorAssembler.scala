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

import io.rml.framework.core.extractors.NodeCache
import io.rml.framework.core.item.Item
import io.rml.framework.core.model.{Entity, Literal, ObjectMap, Uri}
import io.rml.framework.core.vocabulary.R2RMLVoc

class ObjectGeneratorAssembler extends TermMapGeneratorAssembler {

  def assemble(objectMap: ObjectMap, higherLevelLogicalTargetIDs: Set[String]): (Item) => Option[Iterable[Entity]] = {
    val logicalTargetIDs = higherLevelLogicalTargetIDs ++ objectMap.getAllLogicalTargetIds
    // check if it has a parent triple map
    if (objectMap.parentTriplesMap.isDefined) {

      super.assemble(NodeCache.getTriplesMap(objectMap.parentTriplesMap.get).get.subjectMap, logicalTargetIDs)
    } else if (objectMap.hasFunctionMap) {
      val assembledFunction = FunctionMapGeneratorAssembler().assemble(objectMap.functionMap.head, logicalTargetIDs)
      val termTypeString = objectMap.termType.map(_.toString).getOrElse("")
      assembledFunction.andThen(item => {
        if (item.isDefined) {
          termTypeString match {
            case R2RMLVoc.Class.IRI => item.map(iter => iter.map(elem => Uri(elem.value)))   // TODO: this is because a function always returns a Literal (see DynamicFunction)
            case _ => item.map(iter => iter.flatMap(elem => {
              Some(Literal(elem.identifier, objectMap.datatype, objectMap.language))
            }))
          }

        } else {
          None
        }
      })

    } else {
      super.assemble(objectMap, Set())
    }

  }


}

object ObjectGeneratorAssembler {
  def apply(): ObjectGeneratorAssembler = new ObjectGeneratorAssembler()
}
