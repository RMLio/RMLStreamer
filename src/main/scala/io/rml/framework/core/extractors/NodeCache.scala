package io.rml.framework.core.extractors

import io.rml.framework.core.model.{LogicalTarget, Node, TriplesMap}

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
  * */
object NodeCache extends scala.collection.mutable.HashMap[String, Node] {

  def getTriplesMap(resource: String): Option[TriplesMap] = {
    val node = NodeCache.get(resource)
    node match {
      case Some(tm: TriplesMap) => Some(tm)
      case None => None
      case _ => throw new InternalError(s"Expected TriplesMap in node cache for key ${resource}")
    }
  }

  def getLogicalTarget(identifier: String): Option[LogicalTarget] = {
    val node = NodeCache.get(identifier)
    node match {
      case Some(tm: LogicalTarget) => Some(tm)
      case None => None
      case _ => throw new InternalError(s"Expected TriplesMap in node cache for key ${identifier}")
    }
  }

  def logicalTargetIterator: Iterator[(String, LogicalTarget)] = {
    this.iterator
      .filter(entry => entry._2.isInstanceOf[LogicalTarget])
      .map(entry => (entry._1, entry._2.asInstanceOf[LogicalTarget]))
  }
}
