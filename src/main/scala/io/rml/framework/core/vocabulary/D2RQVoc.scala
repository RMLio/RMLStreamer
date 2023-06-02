package io.rml.framework.core.vocabulary

object D2RQVoc {
  val namespace = ("d2rq", "http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#")

  object Property {
    val JDBC_DSN = namespace._2 + "jdbcDSN"
    val JDBC_DRIVER = namespace._2 + "jdbcDriver"
    val USERNAME = namespace._2 + "username"
    val PASSWORD = namespace._2 + "password"
    val QUERY = namespace._2 + ""
  }

  object Class {
    val DATABASE = namespace._2 + "Database"
  }

}
