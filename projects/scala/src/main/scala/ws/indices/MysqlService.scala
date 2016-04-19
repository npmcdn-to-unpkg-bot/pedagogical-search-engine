package ws.indices

import slick.jdbc.JdbcBackend._
import ws.autocomplete.fetcher.Jdbc

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MysqlService {
  // Create the service itself
  val db = Database.forConfig("wikichimp.indices.ws.slick")
  val fetcher = new Jdbc(db)

  // Define the search-strategy
  def search(uris: Set[String], from: Int = 0, to: Int = 9): Future[List[PublicResponse]] = {
    // Some validation
    val validatedUris = uris.map(_.trim).filter(_.length > 0).toSet
    val validatedFrom = math.min(from, to)
    val validatedTo = math.max(from, to)
    val distance = (validatedTo - validatedFrom) + 1

    // Check the minimal conditions for the query to be valid
    if(validatedUris.isEmpty || distance > 20 || distance < 1) {
      Future.successful(Nil)
    } else {
      // Search for the uris
      db.run(Queries.paged(validatedUris, validatedFrom, validatedTo)).flatMap(rows => {
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
              PublicResponse(title, typeCol, href.getOrElse(""), snippet, score)
          }.toList
        }
      }
    }
  }
}
