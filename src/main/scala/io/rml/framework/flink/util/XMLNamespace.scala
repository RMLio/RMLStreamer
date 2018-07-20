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
