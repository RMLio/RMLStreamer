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
package io.rml.framework.flink.util

import java.io.{File, FileInputStream, InputStreamReader}

object XMLNamespace {

  def fromFile(path: String): List[(String, String)] = {
    val file = new File(path)

    val in = new FileInputStream(file)
    val reader = new InputStreamReader(in)
    try {
       fromStreamReader(reader)
    } finally {
      if (in != null) in.close()
      if (reader != null) reader.close()
    }
  }

  def fromStreamReader(reader: InputStreamReader): List[(String, String)] = {
    var bracketCounter = 0

    var buffer = ""
    var c = 0
    while ( {
      c != -1 && bracketCounter != 2
    }) {
      val char = reader.read().asInstanceOf[Char]
      buffer += char
      if (char == '>') bracketCounter += 1
    }


    val namespaceRegex = "xmlns:(.*)=\"([^\"]*\")".r
    val namespaceKeyRegex = "xmlns:(.*)=".r
    val namespaceValueRegex = "\"([^\"]*)\"".r
    val namespaceMap = namespaceRegex.findAllIn(buffer).map(item => {
      val keys = namespaceKeyRegex.findAllIn(item).matchData map {
        m => m.group(1)
      }
      val values = namespaceValueRegex.findAllIn(item).matchData map {
        m => m.group(1)
      }
      (keys.toList.head, values.toList.head)
    }).toList

    namespaceMap
  }
}
