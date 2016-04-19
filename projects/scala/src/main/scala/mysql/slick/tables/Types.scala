package mysql.slick.tables

object Types {
  type Details = (String, String, String, Option[String], String, String)
  type Indices = (String, String, Double)
  type Titles = (String, String, Int)
  type DictionaryDisambiguations = (String, String, String, String, Int, Int)
  type DictionaryRedirects = (String, String, String, Int, Int)
  type DictionaryTitles = (String, String, Int, Int)
}
