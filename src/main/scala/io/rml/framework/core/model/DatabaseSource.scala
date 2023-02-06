package io.rml.framework.core.model

import be.ugent.idlab.knows.dataio.access.DatabaseType

/**
 * Class representing a database connection parsed from the mapping file
 * @param jdbcURL URL to reach the database
 * @param username username to use when accessing the database
 * @param password password corresponding to the username
 * @param dbType type of database being used, characterized by the driver URL
 */
case class DatabaseSource(var jdbcURL: String, username: String, password: String, dbType: DatabaseType) extends StreamDataSource {
  override def uri: ExplicitNode = Uri(jdbcURL)

  def setURL(url: String): Unit = {
    this.jdbcURL = url
  }
}
