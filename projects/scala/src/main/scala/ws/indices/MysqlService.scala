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
  : Future[PublicResponse] = {
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
      Future.successful(PublicResponse(Nil, 0))
    } else {
      // Get the results
      val pair = getAllResults(validatedUris, validatedFrom, validatedTo)

      // Produce the server responses
      pair.map {
        case (results, nbResults) => {
          // Collect the entries
          val entries = results.map {
            case Result(resourceId, entryId, score, title, typeCol, href, oSnippet) => {
              val noSnippet = ""
              val snippet = oSnippet match {
                case None => noSnippet
                case Some(s) => instantiateSnippet(s, uris).getOrElse(noSnippet)
              }
              PublicEntry(title, typeCol, href, snippet, score)
            }
          }.toList

          //
          PublicResponse(entries, nbResults)
        }
      }
    }
  }

  def getAllResults(uris: Set[String], from: Int, to: Int)
  : Future[(Set[Result], Int)] = {
    val start = System.nanoTime()

    // Search for the uris
    db.run(Queries.paged(uris, 0, 500)).map(rows => {
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
      val nbResults = ranked.size

      // Take the indices of interest
      val indices = ranked.drop(from).take(to - from + 1)

      // Build a map between entryIds and (sumScore, resourceId)
      val eiMap = indices.map {
        case Index(eId, sumScore, rId) => eId ->(sumScore, rId)
      }.toMap

      // Fetch the details
      val entryIds = indices.map(_.entryId)
      db.run(Queries.details(entryIds)).map {
        case rows => {
          // Create the results
          val results = rows.map(row => row match {
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

          (results, nbResults)
        }
      }
    })
  }

  def instantiateSnippet(snippetStr: String, uris: Set[String])
  : Option[String] = {
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

    (topSnippet:::pumped) match {
      case Nil => None
      case snippets => Some(org.json4s.native.Serialization.write(snippets))
    }
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
