package mysql.slick.tables

import java.sql.Timestamp

import slick.driver.MySQLDriver.api._

class Messages(tag: Tag)
extends Table[Types.Messages](tag, "messages") {
  def autoId = column[Int]("AutoId", O.PrimaryKey, O.AutoInc)
  def sid = column[Option[Int]]("Sid")
  def category = column[String]("Category")
  def content = column[String]("Content")
  def timestamp = column[Option[Timestamp]]("Timestamp")

  override def * = (autoId, sid, category, content, timestamp)
}
