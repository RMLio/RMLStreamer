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

package io.rml.framework.engine

import io.rml.framework.core.model.Uri
import io.rml.framework.core.model.rdf.RDFTriple
import io.rml.framework.flink.FlinkRDFTriple
import io.rml.framework.flink.item.Item

case class AirplanesMockEngine() extends Engine[Item] {

  var counter = 0

  override def process(item: Item): List[FlinkRDFTriple] = {
    val subj = Uri("http://airdata.io/resource/" + Uri.encode(item.refer("Name").get))
    val pred = Uri("http://airdata.io/ontology/country")
    val obj = Uri("http://airdata.io/resource/" + Uri.encode(item.refer("Country").get))
    counter += 1
    List(RDFTriple(subj, pred, obj))
    null
  }

}