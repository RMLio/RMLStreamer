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

/**
  * class JoinedLocalStatement(index: Int, statement: Statement[Item]) extends Statement[JoinedItems]{
  * override def process(items: JoinedItems): Option[FlinkRDFTriple] = {
  * val currentItem = items(index)
  * val localTriple = statement.process(currentItem)
  * localTriple
  * }
  * }
  * *
  * class JoinedChildStatement(index: Int, statement: ChildStatement[Item]) extends Statement[JoinedItems]{
  * override def process(items: JoinedItems): Option[FlinkRDFTriple] = {
  * val currentItem = items(index)
  * val parentItem = items(index)
  * val triple = statement.process(JoinedItem(currentItem, parentItem))
  * triple
  * }
  * }
  **/
