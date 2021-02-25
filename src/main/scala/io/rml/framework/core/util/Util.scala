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
package io.rml.framework.core.util

import io.rml.framework.api.RMLEnvironment
import io.rml.framework.core.extractors.MappingReader
import io.rml.framework.core.internal.Logging
import io.rml.framework.core.model.{FormattedRMLMapping, Literal, Node, RMLMapping}
import io.rml.framework.shared.ReadException

import java.io._
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.regex.Pattern
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.{Failure, Success, Try}


object Util extends Logging{

  // Without support for custom registered languages of length 5-8 of the IANA language-subtag-registry
  private val regexPatternLanguageTag = Pattern.compile("^((?:(en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang))|((?:([A-Za-z]{2,3}(-(?:[A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4})(-(?:[A-Za-z]{4}))?(-(?:[A-Za-z]{2}|[0-9]{3}))?(-(?:[A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-(?:[0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+))*(-(?:x(-[A-Za-z0-9]{1,8})+))?)|(?:x(-[A-Za-z0-9]{1,8})+))$")

  private val baseDirectiveCapture = "@base <([^<>]*)>.*".r

  def getLiteral(node: Node):Option[Literal]= {
    node match{
      case lit:Literal => Some(lit)
      case _ => None
    }
  }

  /**
    * Check if conforming to https://tools.ietf.org/html/bcp47#section-2.2.9
    *
    * @param s language tag
    * @return True if valid language tag according to BCP 47
    */
  def isValidrrLanguage(s: String): Boolean = regexPatternLanguageTag.matcher(s).matches


  def getFileInputStream(f: File): InputStream = new FileInputStream(f)

  def getFileInputStream(s: String): InputStream = Files.newInputStream(Paths.get(s))


  /**
    * Extract base url from input stream of turtle mapping file
    *
    * @param input inputstream of turtle mapping file
    * @return base url string
    */
  def getBaseDirective(input: InputStream): String = {
    val lineIter = Source.fromInputStream(input).getLines()

    var directives = ListBuffer[String]()
    // breaks aren't advisable to use in scala so we have to do it the hard way with while
    var done = false
    while (lineIter.hasNext && !done) {
      val line = lineIter.next().trim
      if (line.length > 0) {
        if (line.head != '@') {
          done = true
        } else if (line.contains("@base")) {

          directives += line
        }
      }
    }

    if (directives.nonEmpty) {
      getBaseDirective(directives.toList)
    } else {
      ""
    }
  }

  def getBaseDirective(turtleDump: String): String = {
    val stream = new ByteArrayInputStream(turtleDump.getBytes(StandardCharsets.UTF_8))
    getBaseDirective(stream)
  }

  /**
    * Extract the base directive from a list of directives in the
    * turtle file
    *
    * @param directives list of string containing directives of a turtle file
    * @return base directive string defined in the list of directives
    */
  def getBaseDirective(directives: List[String]): String = {
    val filtered = directives.map(regexCaptureBaseUrl).filterNot(_.isEmpty)
    if (filtered.nonEmpty) {
      filtered.head
    } else {
      ""
    }
  }


  /**
    *
    * Extract base url from the base directive string using regex
    *
    * @param directive a base directive string
    * @return base url string
    */
  def regexCaptureBaseUrl(directive: String): String = {
    directive match {
      case baseDirectiveCapture(url) => url
      case _ => ""
    }
  }


  def isValidAbsoluteUri(uri: String): Boolean = {
    try {
      val validatedUri = new URI(uri)
      return validatedUri.getScheme != null
    } catch {
      case e: Throwable => {
        false
      }
    }
  }



  def isRootIteratorTag(tag: String): Boolean = {
    io.rml.framework.flink.source.Source.DEFAULT_ITERATOR_SET.contains(tag)
  }

  // auto-close resources, seems to be missing in Scala
  def tryWith[R, T <: AutoCloseable](resource: T)(doWork: T => R): R = {
    try {
      doWork(resource)
    }
    finally {
      try {
        if (resource != null) {
          resource.close()
        }
      }
      catch {
        case e: Exception => {
          throw new ReadException(e.getMessage)
        }
      }
    }
  }

  def guessFormatFromFileName(fileName: String): Option[Format] = {
    val suffix = fileName.substring(fileName.lastIndexOf('.')).toLowerCase
    suffix match {
      case ".ttl" => Some(Turtle)
      case ".nt" => Some(NTriples)
      case ".nq" => Some(NQuads)
      case ".json" => Some(JSON_LD)
      case ".json-ld" => Some(JSON_LD)
      case _ => None
    }
  }

  /**
    * Utility method for reading a mapping file and converting it to a formatted RML mapping.
    *
    * @param path
    * @return
    */
  def readMappingFile(path: String): FormattedRMLMapping = {
    val mappingFile = getFile(path);
    RMLEnvironment.setMappingFileBaseIRI(Some(mappingFile.getCanonicalPath))
    val mapping = MappingReader().read(mappingFile).asInstanceOf[RMLMapping];
    FormattedRMLMapping.fromRMLMapping(mapping)
  }

  private def getFileUsingClassLoader(path : String) = {
    val classLoader = getClass.getClassLoader
    new File(classLoader.getResource(path).getFile)
  }
  private def getFileRelativeToUserDir(path : String) = {
    val userDir = System.getProperty("user.dir")
    resolveFileRelativeToSourceFileParent(userDir,path)
  }
  /**
    * If the given path is absolute, then a File object representing that path is returned.
    * If the given path is relative, then the following steps are taken to find the file
   *  - 1. using the classloader (a File object representing the path relative to the root class directory is returned.
   *  This can also be a path in a jar.)
   *  - 2. try to find the file relative to the user directory
    * @param path
    * @return
    */
  def getFile(path: String): File = {
    val file_1 = new File(path)
    val result = if (file_1.isAbsolute) {
      new File(path)
    } else {
      // Try to find the file use the class loader
      val out = Try(getFileUsingClassLoader(path)) match {
        case Success(file) => Some(file)
        case Failure(exception) => {
          logWarning(s"can't find file $path using class loader")
          // Try to find the file relative to the user directory
          Try(getFileRelativeToUserDir(path)) match {
            case Success(file) =>  Some(file)
            case Failure(exception) => {
              logWarning(s"can't find file $path relative to working dir")
              None
            }
          }
        }
      }
      out.getOrElse(
       throw new FileNotFoundException(s"Unable to find ${path}")
      )

    }
    result
  }


  def resolveFileRelativeToSourceFileParent(sourcePathString: String, other : String) = {
    val sourcePath = Paths.get(sourcePathString)

    val resolved = if (sourcePath.toFile.isDirectory) {
      sourcePath.resolve(other)
    } else {
      sourcePath.resolveSibling(other)
    };

    resolved.toFile
      .getCanonicalFile
  }



}
