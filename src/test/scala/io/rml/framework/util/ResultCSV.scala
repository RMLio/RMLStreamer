package io.rml.framework.util

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.dataformat.csv.{CsvMapper, CsvSchema}

import scala.collection.JavaConverters


object ResultCSV {

    def writeResult(passingTestCases:Set[String], failingTestCases: Set[String]= Set()):Unit = {

      val mapper = new CsvMapper()
      val schema = CsvSchema.builder()
        .addColumn("testid")
        .addColumn("result")
        .build()

      val csv = mapper.writer(schema).writeValueAsString(JavaConverters.mapAsJavaMapConverter(Map("testid" -> "nklsjdf", "result" -> "jkmlqsdf")).asJava)
      println(csv)
    }


  def main(args: Array[String]): Unit = {
    writeResult(Set())
  }

}


case class Record(var testid:String, var result:String)






