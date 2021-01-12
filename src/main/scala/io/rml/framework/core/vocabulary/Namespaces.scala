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
    RDFVoc.namespace._1 -> RDFVoc.namespace._2,
    RDFSVoc.namespace._1 -> RDFSVoc.namespace._2,
    R2RMLVoc.namespace._1 -> R2RMLVoc.namespace._2,
    FormatVoc.namespace._1 -> FormatVoc.namespace._2,
    QueryVoc.namespace._1 -> QueryVoc.namespace._2,
    RMLVoc.namespace._1 -> RMLVoc.namespace._2,
    RMLSVoc.namespace._1 -> RMLSVoc.namespace._2,
    FunVoc.Fnml.namespace._1 -> FunVoc.Fnml.namespace._2,

    FunVoc.FnO.namespace._1 -> FunVoc.FnO.namespace._2,
    FunVoc.FnOMapping.namespace._1 -> FunVoc.FnOMapping.namespace._2,
    FunVoc.FnoImplementation.namespace._1 -> FunVoc.FnoImplementation.namespace._2,

    FunVoc.GREL.namespace._1 -> FunVoc.GREL.namespace._2,
    LibVoc.namespace._1 -> LibVoc.namespace._2,
    XsdVoc.namespace._1 -> XsdVoc.namespace._2,

    DOAPVoc.namespace._1 -> DOAPVoc.namespace._2,

    // Web of Things
    WoTVoc.ThingDescription.namespace._1 -> WoTVoc.ThingDescription.namespace._2,
    WoTVoc.WoTMQTT.namespace._1 -> WoTVoc.WoTMQTT.namespace._2,
    WoTVoc.WotSecurity.namespace._1 -> WoTVoc.WotSecurity.namespace._2,

    // Hypermedia
    HypermediaVoc.namespace._1 -> HypermediaVoc.namespace._2
    
    // HTTP
    //"http" -> "http://www.w3.org/2011/http#",
    // TODO "htv" -> "http://www.w3.org/2011/http#"  // typically used in WoT documents.
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
