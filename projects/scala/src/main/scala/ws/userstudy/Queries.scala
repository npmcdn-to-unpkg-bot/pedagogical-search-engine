package ws.userstudy

import org.json4s.DefaultFormats
import slick.driver.MySQLDriver.api._
import ws.indices.response.QualityType.Quality
import ws.userstudy.enum.ClassificationType.Classification

import scala.util.hashing.MurmurHash3


object Queries {
  private val searchesTQ = TableQuery[mysql.slick.tables.Searches]
  private val clicksTQ = TableQuery[mysql.slick.tables.Clicks]
  private val classificationTQ = TableQuery[mysql.slick.tables.Classifications]
  implicit val format = DefaultFormats

  def saveSearch(uris: Set[String]) = {
    val searchHash: Int = MurmurHash3.unorderedHash(uris)
    val urisJson: String = org.json4s.native.Serialization.write(uris)

    DBIO.seq(
      searchesTQ += (-1, searchHash, urisJson, None)
    )
  }

  def saveClick(uris: Set[String], entryId: String, rank: Int, quality: Quality) = {
    val searchHash: Int = MurmurHash3.unorderedHash(uris)
    val urisJson: String = org.json4s.native.Serialization.write(uris)

    DBIO.seq(
      clicksTQ += (-1, entryId, searchHash, rank, quality.toString, None)
    )
  }

  def saveCl(uris: Set[String], entryId: String, c: Classification) = {
    val searchHash: Int = MurmurHash3.unorderedHash(uris)

    DBIO.seq(
      classificationTQ += (-1, searchHash, entryId, c.toString, None)
    )
  }
}

