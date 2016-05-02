package mysql.slick.tables

import java.sql.Timestamp

import slick.driver.MySQLDriver.api._

class Searches(tag: Tag)
extends Table[Types.Searches](tag, "searches") {
  def autoId = column[Int]("AutoId", O.PrimaryKey, O.AutoInc)
  def searchHash = column[Int]("SearchHash")
  def uris = column[String]("Uris")
  def timestamp = column[Option[Timestamp]]("Timestamp")

  override def * = (autoId, searchHash, uris, timestamp)
}
