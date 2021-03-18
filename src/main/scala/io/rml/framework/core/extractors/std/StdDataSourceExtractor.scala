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

package io.rml.framework.core.extractors.std

import io.rml.framework.core.extractors.DataSourceExtractor
import io.rml.framework.core.extractors.ExtractorUtil.{extractLiteralFromProperty, extractResourceFromProperty, extractSingleLiteralFromProperty, extractSingleResourceFromProperty}
import io.rml.framework.core.model._
import io.rml.framework.core.model.rdf.RDFResource
import io.rml.framework.core.vocabulary._
import io.rml.framework.shared.RMLException

import java.util.Properties

class StdDataSourceExtractor extends DataSourceExtractor {

  /**
    * Extracts a data source from a resource.
    *
    * @param node Resource to extract data source from.
    * @return
    */
  override def extract(node: RDFResource): DataSource = {

    val property = RMLVoc.Property.SOURCE
    val properties = node.listProperties(property)

    if (properties.size != 1) throw new RMLException(node.uri + ": only one data source allowed.")

    properties.head match {
      case literal: Literal => FileDataSource(literal) // the literal represents a path uri
      case resource: RDFResource => extractDataSourceFromResource(resource)
    }

  }

  /**
    * Retrieves data source properties from a resource that represents a data source.
    *
    * @param resource Resource that represents a data source.
    * @return
    */
  private def extractDataSourceFromResource(resource: RDFResource): DataSource = {
    val property = RDFVoc.Property.TYPE
    val properties = resource.listProperties(property)
    if (properties.size != 1) throw new RMLException(resource.uri + ": type must be given.")
    properties.head match {
      case classResource: RDFResource => classResource.uri match {
        case Uri(RMLSVoc.Class.TCPSOCKETSTREAM) => extractTCPSocketStream(resource)
        case Uri(RMLSVoc.Class.FILESTREAM) => extractFileStream(resource)
        case Uri(RMLSVoc.Class.KAFKASTREAM) => extractKafkaStream(resource)
        case Uri(WoTVoc.ThingDescription.Class.THING) => extractWoTSource(resource)
      }
      case literal: Literal => throw new RMLException(literal.value + ": type must be a resource.")
    }
  }

  private def extractFileStream(resource: RDFResource): StreamDataSource = {
    val path = extractSingleLiteralFromProperty(resource, RMLSVoc.Property.PATH)
    FileStream(path)
  }

  private def extractKafkaStream(resource: RDFResource): StreamDataSource = {
    val broker = extractSingleLiteralFromProperty(resource, RMLSVoc.Property.BROKER)
    val groupId = extractSingleLiteralFromProperty(resource, RMLSVoc.Property.GROUPID)
    val topic = extractSingleLiteralFromProperty(resource, RMLSVoc.Property.TOPIC)

    KafkaStream(List(broker), groupId, topic)
  }

  private def extractTCPSocketStream(resource: RDFResource): StreamDataSource = {
    val hostName = extractSingleLiteralFromProperty(resource, RMLSVoc.Property.HOSTNAME)
    val port = extractSingleLiteralFromProperty(resource, RMLSVoc.Property.PORT)
    TCPSocketStream(hostName, port.toInt)
  }

  private def extractWoTSource(resource: RDFResource): DataSource = {
    // A WoT Thing contains (in our case) a PropertyAffordance, which contains a form describing how to access the real source

    val propertyAffordance = extractSingleResourceFromProperty(resource, WoTVoc.ThingDescription.Property.HASPROPERTYAFFORDANCE);
    val form = extractSingleResourceFromProperty(propertyAffordance, WoTVoc.ThingDescription.Property.HASFORM);

    // extract info from form

    // extract the hypermedia target (~uri)
    val hypermediaTarget = extractSingleLiteralFromProperty(form, HypermediaVoc.Property.HASTARGET);

    // extract the desired content type
    val contentType = extractSingleLiteralFromProperty(form, HypermediaVoc.Property.FORCONTENTTYPE);

    // now check for soure type (MQTT, HTTP, ...)
    val isMQTT = form.hasPredicateWith(WoTVoc.WoTMQTT.namespace._2);
    if (isMQTT) {
      return extractWoTMQTTSource(form, hypermediaTarget, contentType);
    } else {
      throw new RMLException("Unknown Web of Things source defined.")
    }
  }

  private def extractWoTMQTTSource(form: RDFResource, hypermediaTarget: String, contentType: String): DataSource = {
    val controlPacketValue = extractLiteralFromProperty(form, WoTVoc.WoTMQTT.Property.CONTROLPACKETVALUE, "SUBSCRIBE");

    var qosOpt: Option[String] = None;
    var dup: String = "false";
    val mqttOptions = extractResourceFromProperty(form, WoTVoc.WoTMQTT.Property.OPTIONS);
    if (mqttOptions.isDefined) {
      // extract the actual values
      val mqttOptionsResource = mqttOptions.get;
      mqttOptionsResource.getList
        .map(rdfNode => rdfNode.asInstanceOf[RDFResource])
        .foreach(mqttOptionsResource => {
          val optionName = extractSingleLiteralFromProperty(mqttOptionsResource, WoTVoc.WoTMQTT.Property.OPTIONNAME);
          optionName match {
            case "qos" => qosOpt = Some(extractSingleLiteralFromProperty(mqttOptionsResource, WoTVoc.WoTMQTT.Property.OPTIONVALUE));
            case "dup" => dup = "true";
          };
        });
    }
    logDebug("MQTT data source defined in mapping file. hypermediaTarget: " + hypermediaTarget
      + ", contentType: " + contentType + ", dup: " + dup + ", qosOpt: " + qosOpt);
    val mqttProperties = new Properties;
    mqttProperties.put("hypermediaTarget", hypermediaTarget);
    mqttProperties.put("contentType", contentType);
    mqttProperties.put("controlPacketValue", controlPacketValue);
    if (qosOpt.isDefined) {
      mqttProperties.put("qos", qosOpt.get);
    }
    mqttProperties.put("dup", dup); // Java 8 can't handle Scala Boolean objects in a Properties object.
    MQTTStream(mqttProperties)
  }
}
