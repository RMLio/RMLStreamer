package io.rml.framework.core.util
import java.io.{File, FileInputStream, InputStream}
import java.nio.file.{Files, Paths}
import java.util.regex.Pattern

import scala.collection.mutable.ListBuffer
import scala.io.Source


object Util {

  // Without support for custom registered languages of length 5-8 of the IANA language-subtag-registry
  private val regexPatternLanguageTag = Pattern.compile("^((?:(en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang))|((?:([A-Za-z]{2,3}(-(?:[A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4})(-(?:[A-Za-z]{4}))?(-(?:[A-Za-z]{2}|[0-9]{3}))?(-(?:[A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-(?:[0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+))*(-(?:x(-[A-Za-z0-9]{1,8})+))?)|(?:x(-[A-Za-z0-9]{1,8})+))$")

  private val baseDirectiveCapture = "@base <([^<>]*)>.*".r
  /**
    * Check if conforming to https://tools.ietf.org/html/bcp47#section-2.2.9
    *
    * @param s language tag
    * @return True if valid language tag according to BCP 47
    */
  def isValidrrLanguage(s: String): Boolean = regexPatternLanguageTag.matcher(s).matches


  def getFileInputStream(f: File): InputStream = new FileInputStream(f)
  def getFileInputStream(s: String): InputStream = Files.newInputStream(Paths.get( s))


  /**
    * Extract base url from input stream of turtle mapping file
    * @param input inputstream of turtle mapping file
    * @return base url string
    */
  def getBaseDirective(input: InputStream): String=  {
    val lineIter = Source.fromInputStream(input).getLines()

    var directives = ListBuffer[String]()
    // breaks aren't advisable to use in scala so we have to do it the hard way with while
    var done = false
    while(lineIter.hasNext && !done){
      val line = lineIter.next().trim
      if (line.length > 0) {
        if (line.head != '@') {
          done = true
        } else if(line.contains("@base")) {

          directives += line
        }
      }
    }

    regexCaptureBaseUrl(directives.head)
  }
  /**
    * Extract the base directive from a list of directives in the
    * turtle file
    * @param directives list of string containing directives of a turtle file
    * @return base directive string defined in the list of directives
    */
  def getBaseDirective(directives: List[String]): String = {
    directives.map(regexCaptureBaseUrl).filterNot(_.isEmpty).head
  }


  /**
    *
    * Extract base url from the base directive string using regex
    * @param directive a base directive string
    * @return base url string
    */
  def regexCaptureBaseUrl (directive: String): String = {
      directive match{
        case baseDirectiveCapture(url) => url
        case _ => ""
      }
  }
}
