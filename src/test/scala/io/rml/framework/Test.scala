package io.rml.framework

import com.ximpleware.extended.{AutoPilotHuge, VTDGenHuge}
import io.rml.framework.flink.item.Item
import io.rml.framework.flink.source.XMLIterator
import io.rml.framework.flink.util.XMLNamespace
import io.rml.framework.shared.RMLException

object Test extends App{

    println("Testing XML parsing performance.")
    val path = "/home/wmaroy/Downloads/4-5/4000000/persons.xml"
    val xpath = "/persons/person"
    var iterator : Iterator[Option[Item]] = null
    val namespaces: Map[String, String] =  XMLNamespace.fromFile(path).map(tuple => tuple._1 -> tuple._2).toMap

    val vg = new VTDGenHuge // parser for xml

    if (vg.parseFile(path, true,VTDGenHuge.MEM_MAPPED)) {
      // setting up navigator and autopilot, these are needed to stream through the xml
      val vn = vg.getNav
      val ap = new AutoPilotHuge(vn)
      namespaces.foreach(tuple => {
        ap.declareXPathNameSpace(tuple._1, tuple._2)
      })
      // set the xpath expression
      ap.selectXPath(xpath)
      // create the iterator
      iterator = XMLIterator(ap, vn, namespaces)
    } else{
      throw new RMLException("Can't parse XML with VTD.")
    }

    var counter = 0L
    while(iterator.hasNext) {

      if(counter % 1000 == 0) println("Reached " + counter + " records.")
      counter += 1
      iterator.next()
    }


}
