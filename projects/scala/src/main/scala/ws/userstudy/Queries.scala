package ws.userstudy

import org.json4s.DefaultFormats
import slick.driver.MySQLDriver.api._
import ws.indices.response.QualityType.Quality
import ws.indices.spraythings.FilterParameterType.FilterParameter
import ws.userstudy.enum.ClassificationType.Classification

import scala.util.hashing.MurmurHash3


object Queries {
  private val clicksTQ = TableQuery[mysql.slick.tables.Clicks]
  private val classificationTQ = TableQuery[mysql.slick.tables.Classifications]
  implicit val format = DefaultFormats

  def saveClick(uris: Set[String], entryId: String, rank: Int, quality: Quality,
                filter: FilterParameter) = {
    val searchHash: Int = MurmurHash3.unorderedHash(uris)

    DBIO.seq(
      clicksTQ += (-1, entryId, searchHash, filter.toString, rank, quality.toString, None)
    )
  }

  def saveCl(uris: Set[String], entryId: String, c: Classification,
             filter: FilterParameter) = {
    val searchHash: Int = MurmurHash3.unorderedHash(uris)

    DBIO.seq(
      classificationTQ += (-1, searchHash, entryId, filter.toString, c.toString, None)
    )
  }
}

