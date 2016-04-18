package mysql.slick.tables

object Types {
  type Details = (String, String, String, Option[String], String)
  type Indices = (String, String, Double)
  type Titles = (String, String, Int)
}
