package mysql.slick.tables

import slick.driver.MySQLDriver.api._

class DictionaryTitles(tag: Tag)
extends Table[Types.DictionaryTitles](tag, "dictionary-titles") {
  def label = column[String]("label")
  def uri = column[String]("uri")
  def in = column[Int]("in")
  def available = column[Int]("available")

  def * = (label, uri, in, available)
}
