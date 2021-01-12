package io.rml.framework.core.vocabulary

/**
  * MIT License
  *
  * Copyright (C) 2017 - 2021 RDF Mapping Language (RML)
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
  * The R2RML vocabulary
  * https://www.w3.org/TR/r2rml/
  *
  * */
object R2RMLVoc {

  val namespace = ("rr", "http://www.w3.org/ns/r2rml#");
  
  object Property {
    val PREDICATEOBJECTMAP = namespace._2 + "predicateObjectMap";
    val PREDICATE = namespace._2 + "predicate";
    val PREDICATEMAP = namespace._2 + "predicateMap";
    val OBJECT = namespace._2 + "object";
    val OBJECTMAP = namespace._2 + "objectMap";
    val TRIPLESMAP = namespace._2 + "triplesMap";
    val SUBJECTMAP = namespace._2 + "subjectMap";
    val SUBJECT = namespace._2 + "subject";
    val CONSTANT = namespace._2 + "constant";
    val TEMPLATE = namespace._2 + "template";
    val TERMTYPE = namespace._2 + "termType";
    val CLASS = namespace._2 + "class";
    val PARENTTRIPLESMAP = namespace._2 + "parentTriplesMap";
    val JOINCONDITION = namespace._2 + "joinCondition";
    val PARENT = namespace._2 + "parent";
    val CHILD = namespace._2 + "child";
    val GRAPH =  namespace._2 + "graph";
    val GRAPHMAP = namespace._2 + "graphMap";
    val DATATYPE = namespace._2 + "datatype";
    val LANGUAGE = namespace._2 + "language";
    val DEFAULTGRAPH = namespace._2 + "defaultGraph"; 
  }
  
  object Class {
    val PREDICATEOBJECTMAP = namespace._2 + "PredicateObjectMap";
    val OBJECTMAP = namespace._2 + "ObjectMap";
    val TRIPLESMAP = namespace._2 + "TriplesMap";
    val IRI = namespace._2 + "IRI";
    val BLANKNODE = namespace._2 + "BlankNode";
    val LITERAL = namespace._2 + "Literal";
  }
  
}
