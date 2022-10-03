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
  * unofficial vocabulary for RML Streaming
  * 
  * */
object RMLSVoc {
  val namespace = ("rmls", "http://semweb.mmlab.be/ns/rmls#");
  
  object Property {
    ///////////////////////////////////////////////////////////////////////////
    // RMLS: TCP Source
    ///////////////////////////////////////////////////////////////////////////

    val HOSTNAME = namespace._2 + "hostName";
    val PORT = namespace._2 + "port";
    val PATH = namespace._2 + "path";
    val TYPE = namespace._2 + "type";

    ///////////////////////////////////////////////////////////////////////////
    // RMLS: Kafka Source
    ///////////////////////////////////////////////////////////////////////////

    val BROKER = namespace._2 + "broker";
    val GROUPID = namespace._2 + "groupId";
    val TOPIC = namespace._2 + "topic";
    val OFFSET = namespace._2 + "offset";
    val KAFKAVERSION= namespace._2 + "kafkaVersion";


    // RMLS: Joins 
    val JOIN_CONFIG = namespace._2 + "joinConfig";
    val JOIN_TYPE = namespace._2 + "joinType"; 
    val WINDOW_TYPE = namespace._2 + "windowType"; 
  }

  object Class {

    ///////////////////////////////////////////////////////////////////////////
    // RMLS
    ///////////////////////////////////////////////////////////////////////////

    val TCPSOCKETSTREAM = namespace._2 + "TCPSocketStream";
    val FILESTREAM = namespace._2 + "FileStream";
    val KAFKASTREAM = namespace._2 + "KafkaStream";


    val JOIN_CONFIG_MAP = namespace._2 + "JoinConfigMap";

    //RMLS: JOIN TYPES
    val TUMBLING_JOIN_TYPE = namespace._2 + "TumblingJoin";
    val CROSS_JOIN_TYPE = namespace._2 + "CrossJoin"; 
    val DYNAMIC_JOIN_TYPE = namespace._2 + "DynamicJoin"; 


    //RMLS: WINDOW TYPE 
    val DYNAMIC_WINDOW = namespace._2 + "DynamicWindow"; 
    val TUMBLING_WINDOW = namespace._2 + "TumblingWindow"; 
    val SLIDING_WINDOW = namespace._2 + "SlidingWindow"; 
  }
}
