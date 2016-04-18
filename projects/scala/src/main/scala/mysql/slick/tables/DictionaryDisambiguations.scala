package mysql.slick.tables

import slick.driver.MySQLDriver.api._

class DictionaryDisambiguations(tag: Tag)
extends Table[Types.DictionaryDisambiguations](tag, "dictionary-disambiguation") {
  def labelA = column[String]("LabelA")
  def a = column[String]("A")
  def b = column[String]("B")
  def labelB = column[String]("LabelB")
  def inB = column[Int]("InB")
  def available = column[Int]("Available")

  def * = (labelA, a, b, labelB, inB, available)
}
