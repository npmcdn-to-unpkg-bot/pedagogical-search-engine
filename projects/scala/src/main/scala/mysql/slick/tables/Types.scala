package mysql.slick.tables

import java.sql.Timestamp

object Types {
  type Details = (String, String, Option[String], String, String)
  type Indices = (String, String, Double, String, String)
  type Titles = (String, String, Int)
  type DictionaryDisambiguations = (String, String, String, String, Int, Int)
  type DictionaryRedirects = (String, String, String, Int, Int)
  type DictionaryTitles = (String, String, Int, Int)
  type CacheEntry = (Int, Int, String, Int, String, String)
  type CacheDetail = (Int, String, String, String, String, Option[Timestamp])
  type Searches = (Int, Int, Option[Int], String, String, Option[Timestamp])
  type Clicks = (Int, String, Int, Option[Int], Int, Option[Timestamp])
  type Messages = (Int, Option[Int], String, String, Option[Timestamp])
  type Classification = (Int, Int, String, Option[Int], String, Option[Timestamp])
}
