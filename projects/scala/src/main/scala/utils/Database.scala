package utils

import java.sql.DriverManager

class Database(settings: Settings) {
  // Settings
  val ip = settings.Wikipedia.Links.Mysql.ip
  val port = settings.Wikipedia.Links.Mysql.port.toString
  val database = settings.Wikipedia.Links.Mysql.database

  val password = settings.Wikipedia.Links.Mysql.password
  val user = settings.Wikipedia.Links.Mysql.user

  val url = s"jdbc:mysql://$ip:$port/$database"

  def getConnection(): java.sql.Connection = {
    Class.forName("com.mysql.jdbc.Driver").newInstance()
    DriverManager.getConnection(url, user, password)
  }

}
