package mysql.slick.tables

import slick.driver.MySQLDriver.api._

case class Details(tag: Tag)
extends Table[Types.Details](tag, "details-next"){
  def entryId = column[String]("EntryId", O.PrimaryKey)
  def title = column[String]("Title")
  def href = column[Option[String]]("Href")
  def snippet = column[String]("Snippet")
  def TopIndicesJson = column[String]("TopIndicesJson")

  override def * = (entryId, title, href, snippet, TopIndicesJson)
}
