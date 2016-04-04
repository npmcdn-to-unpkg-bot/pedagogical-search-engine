package utils

import java.sql.DriverManager

import org.apache.commons.dbcp2.BasicDataSource

class Database(settings: Settings) {
  // Settings
  val ip = settings.Wikipedia.Links.Mysql.ip
  val port = settings.Wikipedia.Links.Mysql.port.toString
  val database = settings.Wikipedia.Links.Mysql.database

  val password = settings.Wikipedia.Links.Mysql.password
  val user = settings.Wikipedia.Links.Mysql.user

  val url = s"jdbc:mysql://$ip:$port/$database"

  // Create the connection pool
  private val dataSource = new BasicDataSource()
  dataSource.setDriverClassName("com.mysql.jdbc.Driver");
  dataSource.setUrl(url)
  dataSource.setUsername(user);
  dataSource.setPassword(password);

  def getConnection(): java.sql.Connection = {
    // todo: dataSource.getConnection()
    Class.forName("com.mysql.jdbc.Driver").newInstance()
    DriverManager.getConnection(url, user, password)
  }

}
