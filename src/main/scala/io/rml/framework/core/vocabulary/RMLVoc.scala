/*
 * Copyright (c) 2017 Ghent University - imec
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.rml.framework.core.vocabulary

/**
  * Contains the constants of the RML vocabulary.
  */
object RMLVoc {

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
    val CONSTANT = Namespaces("rr", "constant")
    val TEMPLATE = Namespaces("rr", "template")
    val TERMTYPE = Namespaces("rr", "termType")
    val CLASS = Namespaces("rr", "class")
    val PARENTTRIPLESMAP = Namespaces("rr", "parentTriplesMap")
    val JOINCONDITION = Namespaces("rr", "joinCondition")
    val PARENT = Namespaces("rr", "parent")
    val CHILD = Namespaces("rr", "child")
    val GRAPHMAP = Namespaces("rr", "graphMap")
    val DATATYPE = Namespaces("rr", "datatype")
    val LANGUAGE = Namespaces("rr", "language")

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

    val ZOOKEEPER = Namespaces("rmls", "zookeeper")
    val BROKER = Namespaces("rmls", "broker")
    val GROUPID = Namespaces("rmls", "groupId")
    val TOPIC = Namespaces("rlms", "topic")

    ///////////////////////////////////////////////////////////////////////////
    // FNML
    ///////////////////////////////////////////////////////////////////////////

    val FUNCTIONVALUE = Namespaces("fnml", "functionValue")

    ///////////////////////////////////////////////////////////////////////////
    // FNO
    ///////////////////////////////////////////////////////////////////////////

    val EXECUTES = Namespaces("fno", "executes")

  }

  object Class {

    ///////////////////////////////////////////////////////////////////////////
    // RR
    ///////////////////////////////////////////////////////////////////////////
    val PREDICATEOBJECTMAP = Namespaces("rr", "PredicateObjectMap")
    val OBJECTMAP = Namespaces("rr", "ObjectMap")
    val TRIPLEMAP = Namespaces("rr", "TriplesMap")
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

  }

}
