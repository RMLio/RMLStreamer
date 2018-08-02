package io.rml.framework.flink.util

sealed abstract class CSVConfig(val delimiter: Char = ',', val quoteCharacter: Char = '"', val recordDelimiter: String = "\n")

case class DefaultCSVConfig() extends CSVConfig

case class CustomCSVConfig(override val delimiter: Char, override val quoteCharacter: Char,
                           override val recordDelimiter: String ) extends CSVConfig(delimiter, quoteCharacter, recordDelimiter)