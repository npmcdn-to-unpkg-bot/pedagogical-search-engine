package ws.indices

import slick.jdbc.JdbcBackend._
import ws.autocomplete.fetcher.Jdbc

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MysqlService {
  // Create the service itself
  val db = Database.forConfig("wikichimp.indices.ws.slick")
  val fetcher = new Jdbc(db)

  // The strategy tries to have the number of results in this window
  val maximum = 10
  val minimum = 5

  def search(uris: Set[String]): Future[List[PublicResponse]] = {
    // todo: Some validation

    // Check the minimal conditions for the query to be valid
    if(false) { // todo: minimal conditions
      Future.successful(Nil)
    } else {
      // todo: Separate this into its own module
      // Search for the uris
      db.run(Queries.paged(uris)).flatMap(rows => {
        // Get the different entryIds
        val entryIds = rows.map {
          case row => row._1
        }.toList

        // Associate each entryId to its score
        // No key-collision since no two identical entryIds there
        val entryToScores: Map[String, Double] = rows.map {
          case (entryId, oScore) => entryId -> oScore.get
        }.toMap

        // Fetch the details
        db.run(Queries.details(entryIds)).map {
          case details => details.map {
            case detail =>
              // Extand the entry with its score
              (entryToScores(detail._1), detail)
          }
        }
      }).map {
        case rows  => {
          rows.map {
            // Produce the response
            case (score, (entryId, title, typeCol, href, snippet)) =>
              PublicResponse(title, typeCol, href.getOrElse(""), score)
          }.toList
        }
      }
    }
  }
}
