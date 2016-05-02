package mysql.slick.tables

import slick.driver.MySQLDriver.api._
import slick.lifted.Tag

case class CacheEntries(tag: Tag)
extends Table[Types.CacheEntry](tag, "cache-entries") {
  def autoId = column[Int]("AutoId", O.AutoInc, O.PrimaryKey)
  def searchHash = column[Int]("SearchHash")
  def entryId = column[String]("EntryId")
  def rank = column[Int]("Rank")
  def source = column[String]("Source")

  override def * = (autoId, searchHash, entryId, rank, source)
}
