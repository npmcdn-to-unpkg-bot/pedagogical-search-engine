package mysql.slick.tables

import java.sql.Timestamp

import slick.driver.MySQLDriver.api._

class Clicks(tag: Tag)
extends Table[Types.Clicks](tag, "clicks") {
  def autoId = column[Int]("AutoId", O.PrimaryKey, O.AutoInc)
  def entryId = column[String]("EntryId")
  def searchHash = column[Int]("SearchHash")
  def sid = column[Option[Int]]("Sid")
  def rank = column[Int]("Rank")
  def timestamp = column[Option[Timestamp]]("Timestamp")

  override def * = (autoId, entryId, searchHash, sid, rank, timestamp)
}
