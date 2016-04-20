package ws.indices


import org.json4s.{DefaultFormats, FieldSerializer}
import rsc.Formatters
import slick.jdbc.JdbcBackend._
import ws.autocomplete.fetcher.Jdbc
import org.json4s.native.JsonMethods.parse
import rsc.snippets.{Line, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MysqlService extends Formatters {

  implicit val formatter = formats

  // Create the service itself
  val db = Database.forConfig("wikichimp.indices.ws.slick")
  val fetcher = new Jdbc(db)

  // Define the search-strategy
  def search(uris: Set[String], oFrom: Option[Int] = None, oTo: Option[Int] = None)
  : Future[List[PublicResponse]] = {
    // defaults
    val from = oFrom.getOrElse(0)
    val to = oTo.getOrElse(from + 9)

    // Some validation
    val validatedUris = uris.map(_.trim.toLowerCase).filter(_.length > 0)
    val validatedFrom = math.min(from, to)
    val validatedTo = math.max(from, to)
    val distance = (validatedTo - validatedFrom) + 1

    // Check the minimal conditions for the query to be valid
    if(validatedUris.isEmpty || distance > 10 || distance < 1) {
      Future.successful(Nil)
    } else {
      // Get the results
      val allResults = getAllResults(validatedUris, validatedFrom, validatedTo)

      // Produce the server responses
      allResults.map(results => {
        results.map {
          case Result(resourceId, entryId, score, title, typeCol, href, oSnippet) => {
            val snippet = oSnippet match {
              case None => ""
              case Some(s) => instantiateSnippet(s, uris)
            }
            PublicResponse(title, typeCol, href ,snippet, score)
          }
        }.toList
      })
    }
  }

  def getAllResults(uris: Set[String], from: Int, to: Int)
  : Future[Set[Result]] = {
    val start = System.nanoTime()

    // Search for the uris
    db.run(Queries.paged(uris, 0, 500)).map(rs => {
      println("scores: " + utils.Utils.elapsedMs(start) + ", size=" + rs.size)
      rs
    }).map(rows => {
      // Extract the indices
      val indices = rows.map {
        case (entryId, oScore, oResourceId) =>
          Index(entryId, oScore.get, oResourceId.get)
      }

      // Remove the duplicated resources
      val grouped = indices.groupBy(_.resourceId)
      val filtered = grouped.map {
        case (resourceId, group) => group.sortBy(-_.sumScore).head
      }.toList

      // Rank the indices
      filtered.sortBy(-_.sumScore)

    }).flatMap(ranked => {
      // Are there other indices after?
      val lastPage = (ranked.size - 1 <= to)
      // todo: Add this to a context object

      // Take the indices of interest
      val indices = ranked.drop(from).take(to - from + 1)

      // Build a map between entryIds and (sumScore, resourceId)
      val eiMap = indices.map {
        case Index(eId, sumScore, rId) => eId ->(sumScore, rId)
      }.toMap

      // Fetch the details
      val entryIds = indices.map(_.entryId)
      db.run(Queries.details(entryIds)).map(rs => {
        println("details: " + utils.Utils.elapsedMs(start) + ", size=" + rs.size)
        rs
      }).map {
        case rows => rows.map(row => row match {
          case (entryId, title, typeText, oHref, snippet) => {
            // Get the corresponding (sumScore, resourceId) information
            val (sumScore, resourceId) = eiMap(entryId)

            // Produce a result
            val oSnippet = (snippet.size == 0) match {
              case true => None
              case false => Some(snippet)
            }
            Result(resourceId, entryId, sumScore,
              title, typeText, oHref.getOrElse(""), oSnippet)
          }
        }).toSet
      }
    })
  }

  def instantiateSnippet(snippetStr: String, uris: Set[String])
  : String = {
    // Extract the snippet
    val json = parse(snippetStr)
    val snippet = json.extract[rsc.snippets.Snippet]

    //
    val topLine = snippet.topLine
    val topSnippet = (topLine.source == Source.title) match {
      case true => Nil
      case false => List(snippetFromLine(topLine, uris))
    }
    val remaining = 3 - topSnippet.size
    val pumped = pumpNSnippets(remaining, snippet.otherLines, uris)

    org.json4s.native.Serialization.write(topSnippet:::pumped)
  }

  def pumpNSnippets(n: Int, lines: List[Line], uris: Set[String])
  : List[Snippet] = {
    val filtered = lines.filterNot {
      case line => {
        val matched = line.indices.filter(index => uris.contains(index.uri))
        matched.isEmpty
      }
    }
    val tocLines = filtered.filter(_.priority != 1)
    val ordered = tocLines.sortBy(_.priority)
    val topN = ordered.take(n)
    topN.map(snippetFromLine(_, uris))
  }

  def snippetFromLine(line: Line, uris: Set[String])
  : Snippet = {
    val matched = line.indices.filter(index => uris.contains(index.uri))
    val spots = matched.map(index => Spot(index.start, index.stop, index.uri))
    Snippet(line.text, spots)
  }
}
