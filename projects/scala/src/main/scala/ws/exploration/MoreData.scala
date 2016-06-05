package ws.exploration

import slick.driver.MySQLDriver.api._
import slick.jdbc.JdbcBackend.{Database => _}
import slick.jdbc.{PositionedParameters, SetParameter}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object MoreData {
  private val db = Database.forConfig("wikichimp.indices.ws.slick")

  // Handle slick plain-sql "IN clause"
  implicit val setStringListParameter = new SetParameter[Set[String]]{
    def apply(v1: Set[String], v2: PositionedParameters): Unit = {
      v1.foreach(v2.setString)
    }
  }

  def entryScore(uris: Set[String], entryId: String)
  : Future[Option[Double]] = {
    val action = sql"""
    SELECT SUM(Score)
    FROM `indices-old`
    WHERE
      Uri IN ($uris#${",?" * (uris.size - 1)}) AND
      EntryId = '#$entryId'
    GROUP BY EntryId
    ;
    """.as[Double]
    db.run(action).map {
      case vector => vector.isEmpty match {
        case true => None
        case false => Some(vector(0))
      }
    }
  }
}
