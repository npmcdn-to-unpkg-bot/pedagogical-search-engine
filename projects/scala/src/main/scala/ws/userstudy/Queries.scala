package ws.userstudy

import org.json4s.DefaultFormats
import slick.driver.MySQLDriver.api._

import scala.util.hashing.MurmurHash3


object Queries {
  private val searchesTQ = TableQuery[mysql.slick.tables.Searches]
  implicit val format = DefaultFormats

  def saveSearch(uris: Set[String]) = {
    val searchHash: Int = MurmurHash3.unorderedHash(uris)
    val urisJson: String = org.json4s.native.Serialization.write(uris)

    DBIO.seq(
      searchesTQ += (-1, searchHash, urisJson, None)
    )
  }
}

