package ws.tables

import slick.driver.MySQLDriver.api._

class Titles(tag: Tag)
  extends Table[(String, String, Int)](tag, "dictionary-titles") {
  def uri = column[String]("Uri", O.PrimaryKey)
  def label = column[String]("Label")
  def in = column[Int]("In")

  override def * = (uri, label, in)
}
