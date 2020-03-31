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

import java.net.URLEncoder

import io.rml.framework.core.model.std.StdUri

/**
  * This trait represents a Uri.
  */
trait Uri extends TermNode with ExplicitNode{

  def uri : String

  override def identifier: String = this.uri
}


object Uri {

  /**
    *
    * @param uri
    * @return
    */
  def apply(uri: String): Uri = {
    if (uri != null) StdUri(uri) else Uri("")
  }

  def unapply(arg: Uri): Option[String] = Some(arg.toString)

  def encoded(uri: String): Uri = {
    if (uri != null) StdUri(encode(uri)) else Uri("")
  }

  def encode(s: String): String = {
    URLEncoder.encode(s, "UTF-8").replace("+", "%20") // https://stackoverflow.com/questions/4737841/urlencoder-not-able-to-translate-space-character
  }

}
