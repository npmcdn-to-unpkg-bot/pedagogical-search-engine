package mysql.slick.tables

import java.sql.Timestamp

import slick.driver.MySQLDriver.api._

class Searches(tag: Tag)
extends Table[Types.Searches](tag, "searches") {
  def autoId = column[Int]("AutoId", O.PrimaryKey, O.AutoInc)
  def searchHash = column[Int]("SearchHash")
  def sid = column[Option[Int]]("Sid")
  def searchLog = column[String]("SearchLog")
  def resultLog = column[String]("ResultLog")
  def timestamp = column[Option[Timestamp]]("Timestamp")

  override def * = (autoId, searchHash, sid, searchLog, resultLog, timestamp)
}
