package mysql.slick.tables

import java.sql.Timestamp

object Types {
  type Details = (String, String, String, Option[String], String)
  type Indices = (String, String, Double, String)
  type Titles = (String, String, Int)
  type DictionaryDisambiguations = (String, String, String, String, Int, Int)
  type DictionaryRedirects = (String, String, String, Int, Int)
  type DictionaryTitles = (String, String, Int, Int)
  type CacheEntry = (Int, Int, String, Int, String)
  type CacheDetail = (Int, String, String, String, String, String, Option[Timestamp])
}
