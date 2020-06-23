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
  * Contains the constants of the RML vocabulary.
  */
object RMLVoc {

  object Type {
    val XSD_STRING = Namespaces("xsd", "string")
    val XSD_INT = Namespaces("xsd", "int")         // signed 32-bit integer
    val XSD_INTEGER = Namespaces("xsd", "integer") // integer value
    val XSD_DOUBLE = Namespaces("xsd", "decimal")
    val RDF_LIST = Namespaces("rdf", "List")
    val XSD_POSITIVE_INTEGER = Namespaces("xsd", "positiveInteger")
    val XSD_BOOLEAN = Namespaces("xsd", "boolean")

    val XSD_ANY  = Namespaces("xsd", "any")
    val RDF_OBJECT = Namespaces("rdf", "object")

  }

  object Property {

    ///////////////////////////////////////////////////////////////////////////
    // RR
    ///////////////////////////////////////////////////////////////////////////
    val PREDICATEOBJECTMAP = Namespaces("rr", "predicateObjectMap")
    val PREDICATE = Namespaces("rr", "predicate")
    val PREDICATEMAP = Namespaces("rr", "predicateMap")
    val OBJECT = Namespaces("rr", "object")
    val OBJECTMAP = Namespaces("rr", "objectMap")
    val TRIPLESMAP = Namespaces("rr", "triplesMap")
    val SUBJECTMAP = Namespaces("rr", "subjectMap")
    val SUBJECT = Namespaces("rr", "subject")
    val CONSTANT = Namespaces("rr", "constant")
    val TEMPLATE = Namespaces("rr", "template")
    val TERMTYPE = Namespaces("rr", "termType")
    val CLASS = Namespaces("rr", "class")
    val PARENTTRIPLESMAP = Namespaces("rr", "parentTriplesMap")
    val JOINCONDITION = Namespaces("rr", "joinCondition")
    val PARENT = Namespaces("rr", "parent")
    val CHILD = Namespaces("rr", "child")
    val GRAPH =  Namespaces("rr", "graph")
    val GRAPHMAP = Namespaces("rr", "graphMap")
    val DATATYPE = Namespaces("rr", "datatype")
    val LANGUAGE = Namespaces("rr", "language")
    val DEFAULTGRAPH = Namespaces("rr", "defaultGraph")

    ///////////////////////////////////////////////////////////////////////////
    // RML
    ///////////////////////////////////////////////////////////////////////////
    val REFERENCE = Namespaces("rml", "reference")
    val LOGICALSOURCE = Namespaces("rml", "logicalSource")
    val ITERATOR = Namespaces("rml", "iterator")
    val REFERENCEFORMULATION = Namespaces("rml", "referenceFormulation")
    val SOURCE = Namespaces("rml", "source")

    ///////////////////////////////////////////////////////////////////////////
    // RMLS: TCP Source
    ///////////////////////////////////////////////////////////////////////////

    val HOSTNAME = Namespaces("rmls", "hostName")
    val PORT = Namespaces("rmls", "port")
    val PATH = Namespaces("rmls", "path")
    val TYPE = Namespaces("rmls", "type")

    ///////////////////////////////////////////////////////////////////////////
    // RMLS: Kafka Source
    ///////////////////////////////////////////////////////////////////////////

    val BROKER = Namespaces("rmls", "broker")
    val GROUPID = Namespaces("rmls", "groupId")
    val TOPIC = Namespaces("rmls", "topic")
    val KAFKAVERSION= Namespaces("rmls", "kafkaVersion")


    ///////////////////////////////////////////////////////////////////////////
    // GREL
    ///////////////////////////////////////////////////////////////////////////
    val GREL_RANDOM = Namespaces("grel", "random")
    val GREL_UPPERCASE = Namespaces("grel", "toUpperCase")


    ///////////////////////////////////////////////////////////////////////////
    // FNML
    ///////////////////////////////////////////////////////////////////////////

    val FUNCTIONVALUE = Namespaces("fnml", "functionValue")

    ///////////////////////////////////////////////////////////////////////////
    // FNO
    ///////////////////////////////////////////////////////////////////////////

    val EXECUTES = Namespaces("fno", "executes")
    val FNO_SOLVES = Namespaces("fno", "solves")
    val FNO_IMPLEMENTS = Namespaces("fno", "implements")
    val FNO_PREDICATE = Namespaces("fno", "predicate")
    val FNO_EXPECTS = Namespaces("fno", "expects")
    val FNO_RETURNS = Namespaces("fno", "returns")
    val FNO_TYPE = Namespaces("fno", "type")

    val FNO_IMPLEMENTATION = Namespaces("fno", "implementation")

    val FNO_FUNCTION = Namespaces("fno", "function")

    val FNO_METHOD_MAPPING = Namespaces("fno", "methodMapping")

    ///////////////////////////////////////////////////////////////////////////
    // LIB
    ///////////////////////////////////////////////////////////////////////////

    val LIB_PROVIDED_BY = Namespaces("lib", "providedBy")
    val LIB_CLASS = Namespaces("lib", "class")
    val LIB_METHOD = Namespaces("lib", "method")
    val LIB_LOCAL_LIBRARY = Namespaces("lib", "localLibrary")

    ///////////////////////////////////////////////////////////////////////////
    // FNOM(apping)
    ///////////////////////////////////////////////////////////////////////////

    val FNOM_METHOD_NAME = Namespaces("fnom", "method-name")



    ///////////////////////////////////////////////////////////////////////////
    // FNOI
    ///////////////////////////////////////////////////////////////////////////
    val FNOI_CLASS_NAME = Namespaces("fnoi", "class-name")

    ///////////////////////////////////////////////////////////////////////////
    // DOAP
    ///////////////////////////////////////////////////////////////////////////

    val DOAP_DOWNLOAD_PAGE = Namespaces("doap", "download-page")



  }

  object Class {

    ///////////////////////////////////////////////////////////////////////////
    // RR
    ///////////////////////////////////////////////////////////////////////////
    val PREDICATEOBJECTMAP = Namespaces("rr", "PredicateObjectMap")
    val OBJECTMAP = Namespaces("rr", "ObjectMap")
    val TRIPLESMAP = Namespaces("rr", "TriplesMap")
    val IRI = Namespaces("rr", "IRI")
    val BLANKNODE = Namespaces("rr", "BlankNode")
    val LITERAL = Namespaces("rr", "Literal")

    ///////////////////////////////////////////////////////////////////////////
    // QL
    ///////////////////////////////////////////////////////////////////////////

    val JSONPATH = Namespaces("ql", "JSONPath")
    val CSV = Namespaces("ql", "CSV")
    val XPATH = Namespaces("ql", "XPath")

    ///////////////////////////////////////////////////////////////////////////
    // RMLS
    ///////////////////////////////////////////////////////////////////////////

    val TCPSOCKETSTREAM = Namespaces("rmls", "TCPSocketStream")
    val FILESTREAM = Namespaces("rmls", "FileStream")
    val KAFKASTREAM = Namespaces("rmls", "KafkaStream")

    ///////////////////////////////////////////////////////////////////////////
    // FNML
    ///////////////////////////////////////////////////////////////////////////

    val FUNCTIONTERMMAP = Namespaces("fnml", "FunctionTermMap")

    ///////////////////////////////////////////////////////////////////////////
    // FNO
    ///////////////////////////////////////////////////////////////////////////
    val FNO_FUNCTION = Namespaces("fno", "Function")
    val FNO_PARAMETER = Namespaces("fno", "Parameter")
    val FNO_EXECUTION = Namespaces("fno", "Execution")
    val FNO_OUTPUT = Namespaces("fno", "Output")
    val FNO_ALGORITHM = Namespaces("fno", "Algorithm")
    val FNO_PROBLEM = Namespaces("fno", "Problem")

    val FNO_MAPPING = Namespaces("fno", "Mapping")
    val FNO_METHOD_MAPPING = Namespaces("fno", "MethodMapping")

    ///////////////////////////////////////////////////////////////////////////
    // FNOI
    ///////////////////////////////////////////////////////////////////////////

    val FNOI_JAVA_CLASS = Namespaces("fnoi", "JavaClass")

    ///////////////////////////////////////////////////////////////////////////
    // FNOM(apping)
    ///////////////////////////////////////////////////////////////////////////

    val FNOM_STRING_METHOD_MAPPING = Namespaces("fnom", "StringMethodMapping")

  }

}
