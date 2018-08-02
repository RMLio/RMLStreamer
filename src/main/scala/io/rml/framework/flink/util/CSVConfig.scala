package io.rml.framework.flink.util

sealed abstract class CSVConfig(val delimiter:Char, val quoteCharacter:Char)
case class DefaultCSVConfig() extends CSVConfig(',', '"')
case class CustomCSVConfig(override val delimiter:Char, override val quoteCharacter:Char) extends CSVConfig(delimiter , quoteCharacter )