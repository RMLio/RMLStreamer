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

import io.rml.framework.core.model.std.StdLiteral

/**
  * This trait represents a Literal.
  */
trait Literal extends Entity  with ExplicitNode{

  /**
    *
    * @return
    */
  def value: String

  /**
    *
    * @return
    */
  def `type`: Option[Uri]

  /**
    *
    * @return
    */
  def language: Option[Literal]

  /**
    *
    * @return
    */
  override def toString: String = value

  override def identifier: String = value

}

object Literal {

  def apply(value: String, `type`: Option[Uri] = None, language: Option[Literal] = None): Literal = {
    StdLiteral(clean(value), `type`, language)
  }

  def clean(s: String): String = {
    if (s.isEmpty) return ""

    // replace characters
    s.flatMap {
      case '\n' => "\\n"
      case '"' => "\\\""
      case '\\' => "\\\\"
      case '\r' => "\\r"
      case '\b' => "\\b"
      case '\t' => "\\t"
      case '\f' => "\\f"
      case any => any.toString
    }
  }

}
