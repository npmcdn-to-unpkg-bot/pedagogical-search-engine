package mysql.slick.tables

import java.sql.Timestamp

import slick.driver.MySQLDriver.api._

class Classifications(tag: Tag)
extends Table[Types.Classification](tag, "classifications") {
  def autoId = column[Int]("AutoId", O.PrimaryKey, O.AutoInc)
  def searchHash = column[Int]("SearchHash")
  def entryId = column[String]("EntryId")
  def classification = column[String]("Classification")
  def timestamp = column[Option[Timestamp]]("Timestamp")

  override def * = (autoId, searchHash, entryId, classification, timestamp)
}
