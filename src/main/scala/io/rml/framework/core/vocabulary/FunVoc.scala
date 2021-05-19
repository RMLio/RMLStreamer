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
  * Function Ontology
  * https://fno.io
  * https://fno.io/ontology/index-en.html
  *
  * */
object FunVoc {

  /////////////////////
  // FNO
  // Function Ontology
  /////////////////////
  object FnO {
    val namespace = ("fno", "https://w3id.org/function/ontology#");

    object Property {
      val EXECUTES = namespace._2 + "executes";
      val FNO_SOLVES = namespace._2 + "solves";
      val FNO_IMPLEMENTS = namespace._2 + "implements";
      val FNO_PREDICATE = namespace._2 + "predicate";
      val FNO_EXPECTS = namespace._2 + "expects";
      val FNO_RETURNS = namespace._2 + "returns";
      val FNO_TYPE = namespace._2 + "type";

      val FNO_IMPLEMENTATION = namespace._2 + "implementation";

      val FNO_FUNCTION = namespace._2 + "function";

      val FNO_METHOD_MAPPING = namespace._2 + "methodMapping";
    }
    
    object Class {
      val FNO_FUNCTION = namespace._2 + "Function";
      val FNO_PARAMETER = namespace._2 + "Parameter";
      val FNO_EXECUTION = namespace._2 + "Execution";
      val FNO_OUTPUT = namespace._2 + "Output";
      val FNO_ALGORITHM = namespace._2 + "Algorithm";
      val FNO_PROBLEM = namespace._2 + "Problem";

      val FNO_MAPPING = namespace._2 + "Mapping";
      val FNO_METHOD_MAPPING = namespace._2 + "MethodMapping";
    }
  }

  //////////////////////////////////////
  // FNOM
  // Function Ontology Method Mappings
  //////////////////////////////////////
  object FnOMapping {
    val namespace = ("fnom", "https://w3id.org/function/vocabulary/mapping#");

    object Property {
      val FNOM_METHOD_NAME = namespace._2 + "method-name";
    }

    object Class {
      val FNOM_STRING_METHOD_MAPPING = namespace._2 + "StringMethodMapping";
    }
  }

  object FnoImplementation {
    val namespace = ("fnoi", "https://w3id.org/function/vocabulary/implementation#");
    
    object Property {
      val FNOI_CLASS_NAME = namespace._2 + "class-name";
    }

    object Class {
      val FNOI_JAVA_CLASS = namespace._2 + "JavaClass";
    }
  }

  //////////////////////////////////////////////
  // FNML
  // Function RML Extension
  // http://semweb.mmlab.be/ns/fnml/fnml.html
  //////////////////////////////////////////////
  object Fnml {
    val namespace = ("fnml", "http://semweb.mmlab.be/ns/fnml#");

    object Property {
      val FUNCTIONVALUE = namespace._2 + "functionValue";
    }
    
    object Class {
      val FUNCTIONTERMMAP = namespace._2 + "FunctionTermMap";
    }
  }

  /////
  // GREL functions
  // https://fno.io
  // https://github.com/OpenRefine/OpenRefine/wiki/General-Refine-Expression-Language
  // 
  object GREL {
    val namespace = ("grel", "http://users.ugent.be/~bjdmeest/function/grel.ttl#");

    object Property {
      val GREL_RANDOM = namespace._2 + "random";
      val GREL_UPPERCASE = namespace._2 + "toUpperCase";
      val GREL_VALUEPARAMETER = namespace._2 + "valueParameter";
    }
  }

}
