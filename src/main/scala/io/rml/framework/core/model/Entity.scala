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

package io.rml.framework.core.model

/**
  *  RML entity representing the uri, blank or literal nodes.
  */
trait Entity extends Node

object Entity {

  /**
    * Utility method for cleaning up values with a predefined configuration.
    *
    * @param s
    * @return
    */
  def clean(s: String, replace: Boolean = true): String = {
    if (s.isEmpty) return ""
    // characters to be removed from the string
    val toBeRemoved = Array('"')
    // characters to be replaced with a predefined character
    val replaceMap = Map() //Map(' ' -> '_')

    // filter out characters
    val filtered: String = s.filter(character => !toBeRemoved.contains(character))
    // replace characters
    if (replace) {
      replaceMap.foldLeft(filtered)((a, b) => a.replaceAll(b._1.toString, b._2.toString))
    } else filtered

  }
}
