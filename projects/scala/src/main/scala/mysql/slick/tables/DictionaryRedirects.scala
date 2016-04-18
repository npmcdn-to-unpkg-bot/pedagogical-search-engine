package mysql.slick.tables

import slick.driver.MySQLDriver.api._

class DictionaryRedirects(tag: Tag)
extends Table[Types.DictionaryRedirects](tag, "dictionary-redirects"){
  def labelA = column[String]("LabelA")
  def labelB = column[String]("LabelB")
  def uriB = column[String]("UriB")
  def inB = column[Int]("InB")
  def available = column[Int]("Available")

  def * = (labelA, labelB, uriB, inB, available)
}
