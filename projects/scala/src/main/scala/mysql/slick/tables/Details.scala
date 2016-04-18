package mysql.slick.tables

import slick.driver.MySQLDriver.api._

case class Details(tag: Tag)
extends Table[Types.Details](tag, "details"){
  def entryId = column[String]("EntryId", O.PrimaryKey)
  def title = column[String]("Title")
  def typeCol = column[String]("Type")
  def href = column[Option[String]]("Href")
  def snippet = column[String]("Snippet")

  override def * = (entryId, title, typeCol, href, snippet)
}
