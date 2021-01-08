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
  * */

/**
  * Contains the constants of the Web of Things vocabulary.
  */
object WoTVoc {

  ///////////////////////////////////////////////////////////////////////////
  // TD (https://www.w3.org/2019/wot/td)
  ///////////////////////////////////////////////////////////////////////////
  object ThingDescription {
    val namespace = ("td", "https://www.w3.org/2019/wot/td#")

    object Property {
      val HASPROPERTYAFFORDANCE = Namespaces("td", "hasPropertyAffordance")
      val HASFORM = Namespaces("td", "hasForm")
      val HASTARGET = Namespaces("td", "hasTarget")
      val HASCONTENTTYPE = Namespaces("td", "forContentType")
      val HASSECURITYCONFIGURATION = Namespaces("td", "hasSecurityConfiguration")
    }

    object Class {
      val THING = Namespaces("td", "Thing")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // MQV -- MQTT vocabulary.
  // in the making so unofficial (https://www.w3.org/TR/2020/NOTE-wot-binding-templates-20200130/#mqtt-vocabulary)
  ///////////////////////////////////////////////////////////////////////////
  object WoTMQTT {
    val namespace = ("mqv", "http://www.example.org/mqtt-binding#") // TODO: change once an officlial vocabulary is published

    object Property {
      val CONTROLPACKETVALUE = Namespaces("mqv", "controlPacketValue")
      val OPTIONS = Namespaces("mqv", "options")
      val OPTIONNAME = Namespaces("mqv", "optionName")
      val OPTIONVALUE = Namespaces("mqv", "optionValue")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // WOTSEC (https://www.w3.org/2019/wot/security)
  ///////////////////////////////////////////////////////////////////////////
  object WotSecurity {
    val namespace = ("wotsec", "https://www.w3.org/2019/wot/security#")

    object Property {
      val IN = Namespaces("wotsec", "in")
    }

    object Class {
      val BASICSECURITYSCHEME = Namespaces("wotsec", "BasicSecurityScheme")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // HCTL -- Hypermedia Controls Vocabulary (https://www.w3.org/2019/wot/hypermedia)
  ///////////////////////////////////////////////////////////////////////////
  object Hypermedia {
    val namespace = ("hctl", "https://www.w3.org/2019/wot/hypermedia#")

    object Property {
      val OPERATIONTYPE = Namespaces("hctl", "hasOperationType")
    }
  }
}
