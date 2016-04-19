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
  def search(uris: Set[String], from: Int = 0, to: Int = 9): Future[List[PublicResponse]] = {
    // Some validation
    val validatedUris = uris.map(_.trim.toLowerCase).filter(_.length > 0).toSet
    val validatedFrom = math.min(from, to)
    val validatedTo = math.max(from, to)
    val distance = (validatedTo - validatedFrom) + 1

    // Check the minimal conditions for the query to be valid
    if(validatedUris.isEmpty || distance > 20 || distance < 1) {
      Future.successful(Nil)
    } else {
      // Gather the results
      val allResults = getAllResults(validatedUris, validatedFrom, validatedTo)

      // Ensure that not two entries (in the results) belong to the same resource
      val filtered = filterDuplicates(allResults)

      // Produce the public responses
      filtered.map(results => results.map(toPublicResponse(_, validatedUris)).toList)
    }
  }

  def toPublicResponse(result: Result, uris: Set[String])
  : PublicResponse = result match {
    case Result(resourceId, entryId, score, title, typeCol, href, snippet) => {
      PublicResponse(title, typeCol, href,
        instantiateSnippet(snippet, uris), score)
    }
  }

  def getAllResults(uris: Set[String], from: Int, to: Int)
  : Future[Set[Result]] = {
    // Search for the uris
    db.run(Queries.paged(uris, from, to)).flatMap(rows => {
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
          case (score, (entryId, title, typeCol, href, snippet, resourceId)) =>
            Result(resourceId, entryId, score, title,
              typeCol, href.getOrElse(""), snippet)
        }.toSet
      }
    }
  }

  def filterDuplicates(future: Future[Set[Result]])
  : Future[Set[Result]] = future.map(results => {
    val grouped = results.groupBy(_.resourceId)
    val skimmed = grouped.map {
      case (resourceId, setOfResults) => setOfResults.maxBy(_.score)
    }
    skimmed.toSet
  })

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
