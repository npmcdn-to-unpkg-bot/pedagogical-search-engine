package utils

import mysql.Connection

class Amadeus {
  def getConnection(settings: Settings): Connection = {
    return new Connection(
      settings.Wikipedia.Links.Mysql.database,
      settings.Wikipedia.Links.Mysql.user,
      settings.Wikipedia.Links.Mysql.password,
      settings.Wikipedia.Links.Mysql.ip,
      settings.Wikipedia.Links.Mysql.port.toString
    )
  }
}
