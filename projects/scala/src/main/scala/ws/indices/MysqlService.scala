package ws.indices


import org.json4s.{DefaultFormats, FieldSerializer}
import rsc.Formatters
import slick.jdbc.JdbcBackend._
import ws.autocomplete.fetcher.Jdbc
import org.json4s.native.JsonMethods.parse
import rsc.snippets.{Line, Source}
import utils.Logger
import ws.indices.bing.BingFetcher

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MysqlService extends Formatters {

  implicit private val formatter = formats

  // Create the service itself
  private val db = Database.forConfig("wikichimp.indices.ws.slick")
  private val fetcher = new Jdbc(db)

  private val bingFetcher = new BingFetcher(timeoutMs = 2000)

  // Some constants
  private val N_MAX = 500

  // Define the search-strategy
  def search(uris: List[String], oFrom: Option[Int] = None, oTo: Option[Int] = None)
  : Future[PublicResponse] = {
    // defaults
    val from = oFrom.getOrElse(0)
    val to = oTo.getOrElse(from + 4)
    val distance = to - from + 1

    // First validations: discard huge inputs
    if(uris.size > 10 ||
      from > to || from < 0 || to >= N_MAX ||
      distance < 1 || distance > 5) {
      Future.successful(PublicResponse(Nil, 0))

    } else {
      // The input sounds reasonable but perform a in-depth check
      val validatedUris = uris.map(_.trim.toLowerCase).filter(_.length > 0).toSet

      if(validatedUris.isEmpty) {
        Future.successful(PublicResponse(Nil, 0))
      } else {
        fetch(validatedUris, from, to)
      }
    }
  }

  def fetch(uris: Set[String], from: Int, to: Int)
  : Future[PublicResponse] = {

    // Merge the tasks
    val merged = Future.sequence(List(
      // wikichimp
      fetchWikichimpResults(uris, from, to),

      // bing
      fetchBingResults(uris, from, to).recover {
        // In case of error
        case e => {
          val msg = e.getMessage
          Logger.info(s"Failed to fetch bing results but that's OK [recovering from it]. Reason: $msg")

          // Create an empty reponse (as if there were no results)
          PublicResponse(Nil, 0)
        }
      }
    ))

    // When they are done
    merged.map {
      case publicResponses => {
        // How many results do we have in total?
        // This is tricky to say because we do not know the answer for bing results
        val nbResults = publicResponses match {
          case wikichimp::bing::Nil => wikichimp.nbResults
        }

        // Mix their results
        val responses = publicResponses.flatMap(_.entries)
        val grouped = responses.groupBy(_.rank)
        val sorted = grouped.toList.sortBy(_._1)

        val entries = sorted.foldLeft((Quality.high, 0, List[PublicEntry]())){
          case ((lastQuality, rank, acc), (_, entries)) => entries match {
            case e1::e2::Nil => {
              // Deduce the quality if unknown
              val quality = e1.quality match {
                case Quality.unknown => e2.quality
                case _ => e1.quality
              }

              // Randomly choose an order
              val newEntries = (math.random > 0.5) match {
                case true => List(e1.copy(rank = rank, quality = quality), e2.copy(rank = rank + 1, quality = quality))
                case false => List(e2.copy(rank = rank, quality = quality), e1.copy(rank = rank + 1, quality = quality))
              }

              (quality, rank + 2, acc:::newEntries)
            }
            case e1::Nil => {
              // Deduce the quality if unknown
              val quality = e1.quality match {
                case Quality.unknown => lastQuality
                case _ => e1.quality
              }

              (quality, rank + 1, acc:::List(e1.copy(rank = rank, quality = quality)))
            }
          }
        }._3

        // Produce the response
        PublicResponse(entries, nbResults)
      }
    }
  }

  def fetchBingResults(uris: Set[String], from: Int, to: Int)
  : Future[PublicResponse] = {
    //
    val text = uris.take(5).mkString(" ")
    val future = bingFetcher.search(text, from, to)

    // Create the entries
    future.map {
      case results => {
        // Generate the entries
        val entries = results.d.results.zipWithIndex.map {
          case (result, index) => {
            val snippet = Snippet(result.description, Nil)

            PublicEntry(
              beautifyBingTitle(result.title),
              getTypeText(result.url),
              result.url,
              org.json4s.native.Serialization.write(snippet),
              Quality.unknown,
              index
            )
          }
        }

        // .. and public response
        PublicResponse(entries, entries.size)
      }
    }
  }

  def getTypeText(url: String): String = {
    if(url.contains("coursera.org")) {
      "Coursera"
    } else if(url.contains("mit.edu")) {
      "MIT"
    } else if(url.contains("safaribooksonline.com")) {
      "Book"
    } else if(url.contains("scholarpedia.org")) {
      "Scholarpedia"
    } else if(url.contains("khanacademy.org")) {
      "Khanacademy"
    } else {
      "Web"
    }
  }

  def beautifyBingTitle(title: String): String = {
    title
      .replace("| Khan Academy", "")
      .replace("- Khan Academy", "")

      .replace("| Coursera", "")
      .replace("- Coursera", "")

      .replace("| MIT OpenCourseWare", "")
      .replace("- MIT OpenCourseWare", "")

      .replace("| Scholarpedia", "")
      .replace("- Scholarpedia", "")

      .replace("- Safari Books", "")
      .replace("- Safari", "")

      .trim
  }

  def fetchWikichimpResults(uris: Set[String], from: Int, to: Int)
  : Future[PublicResponse] = {
    // Produce the server responses
    getAllResults(uris, from, to).map {
      case (results, nbResults) => {
        // Order the results
        val ordered = results.toList.sortBy(-_.score).zipWithIndex

        // Collect the entries
        val entries = ordered.map {
          case (Result(resourceId, entryId, score, title, typeCol, href, oSnippet), index) => {
            val noSnippet = ""
            val snippet = oSnippet match {
              case None => noSnippet
              case Some(s) => instantiateSnippet(s, uris).getOrElse(noSnippet)
            }
            val quality = (score / uris.size.toDouble) match {
              case small if small < 0.5 => Quality.low
              case medium if medium < 0.8 => Quality.medium
              case high => Quality.high
            }

            PublicEntry(title, typeCol, href, snippet, quality, index)
          }
        }

        //
        PublicResponse(entries, nbResults)
      }
    }
  }

  def getAllResults(uris: Set[String], from: Int, to: Int)
  : Future[(Set[Result], Int)] = {
    val start = System.nanoTime()

    // Search for the uris
    db.run(Queries.paged(uris, 0, N_MAX)).map(rows => {
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
