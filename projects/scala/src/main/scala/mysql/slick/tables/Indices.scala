package mysql.slick.tables

import slick.driver.MySQLDriver.api._

class Indices(tag: Tag)
extends Table[Types.Indices](tag, "indices") {
  def uri = column[String]("Uri")
  def entryId = column[String]("EntryId")
  def score = column[Double]("Score")

  override def * = (uri, entryId, score)
}
