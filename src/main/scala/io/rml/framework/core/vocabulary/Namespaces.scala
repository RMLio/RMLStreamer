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

package io.rml.framework.core.vocabulary

/**
  * This object stores all prefixes and their associated namespaces
  */
object Namespaces {

  private val _namespaces: Map[String, String] = Map(
    "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs" -> "http://www.w3.org/2000/01/rdf-schema#",
    "rr" -> "http://www.w3.org/ns/r2rml#",
    "ql" -> "http://semweb.mmlab.be/ns/ql#",
    "rml" -> "http://semweb.mmlab.be/ns/rml#",
    "rmls" -> "http://semweb.mmlab.be/ns/rmls#",
    "fnml" -> "http://semweb.mmlab.be/ns/fnml#",
    "fno" -> "http://w3id.org/function/ontology#"
  )

  /**
    * Retrieves the Uri in string format that is associated with the given prefix.
    *
    * @param prefix Prefix to get the associated Uri string from.
    * @return
    */
  def apply(prefix: String): String = _namespaces(prefix)

  /**
    * Retrieves the Uri in string format that is associated with the given prefix
    * and appends the given suffix.
    *
    * @param prefix Prefix to get the associated Uri string from.
    * @param suffix Suffix to append to the retrieved Uri.
    * @return
    */
  def apply(prefix: String, suffix: String): String = _namespaces(prefix) + suffix

}
