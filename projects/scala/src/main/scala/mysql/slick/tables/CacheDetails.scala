package mysql.slick.tables

import java.sql.Timestamp

import slick.driver.MySQLDriver.api._

case class CacheDetails(tag: Tag)
extends Table[Types.CacheDetail](tag, "cache-details") {
  def autoId = column[Int]("AutoId", O.PrimaryKey, O.AutoInc)
  def entryId = column[String]("EntryId")
  def title = column[String]("Title")
  def url = column[String]("Url")
  def snippet = column[String]("Snippet")
  def timestamp = column[Option[Timestamp]]("Timestamp")

  override def * = (autoId, entryId, title, url, snippet, timestamp)
}
