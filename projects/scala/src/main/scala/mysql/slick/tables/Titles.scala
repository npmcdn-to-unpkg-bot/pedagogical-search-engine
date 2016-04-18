package mysql.slick.tables

import slick.driver.MySQLDriver.api._

class Titles(tag: Tag)
  extends Table[Types.Titles](tag, "dictionary-titles") {
  def uri = column[String]("Uri", O.PrimaryKey)
  def label = column[String]("Label")
  def in = column[Int]("In")

  override def * = (uri, label, in)
}
