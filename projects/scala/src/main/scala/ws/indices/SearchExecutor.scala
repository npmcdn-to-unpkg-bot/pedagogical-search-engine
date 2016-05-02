package ws.indices


import slick.jdbc.JdbcBackend._
import ws.indices.response.{Entry, QualityType, Response}
import ws.indices.bing.BingFetcher
import ws.indices.enums.WebsiteSourceType
import ws.indices.indexentry.{FullBing, FullWikichimp}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SearchExecutor {

  // Create the service itself
  private val db = Database.forConfig("wikichimp.indices.ws.slick")

  private val bingFetcher = new BingFetcher(timeoutMs = 5000)

  private val indicesFetcher = new IndicesFetcher(db, bingFetcher)
  private val detailsFetcher = new DetailsFetcher(db)

  // Some constants
  private val N_MAX = 500

  // Define the search-strategy
  def search(uris: List[String], oFrom: Option[Int] = None, oTo: Option[Int] = None)
  : Future[Response] = {
    // defaults
    val from = oFrom.getOrElse(0)
    val to = oTo.getOrElse(from + 9)
    val distance = to - from + 1

    // First validations: discard huge inputs
    if(uris.size > 10 ||
      from > to || from < 0 || to >= N_MAX ||
      distance < 1 || distance > 10) {
      Future.successful(Response(Nil, 0))

    } else {
      // The input sounds reasonable but perform a in-depth check
      val validatedUris = uris.map(_.trim.toLowerCase).filter(_.length > 0).toSet

      if(validatedUris.isEmpty) {
        Future.successful(Response(Nil, 0))
      } else {
        unvalidatedSearch(validatedUris, from, to)
      }
    }
  }

  def unvalidatedSearch(uris: Set[String], from: Int, to: Int)
  : Future[Response] = {

    // Fetch all the best indices (without their details yet)
    indicesFetcher.wcAndBing(uris, N_MAX).flatMap(indexEntries => {
      // fetch the details in the [from, to] interval
      val interval = indexEntries.slice(from, to + 1)
      detailsFetcher.resolve(interval, uris).map(r => (r, indexEntries.size))

    }).map {
      case (indices, totalNb) =>
        // create the public entries
        val entries: List[response.Entry] = indices.zipWithIndex.map {
          case (FullBing(_, _, title, source, url, snippet, _), rank) =>
            Entry(
              title,
              WebsiteSourceType.toPublicString(source),
              url,
              snippet.toJSONString,
              QualityType.unknown,
              rank
            )
          case (FullWikichimp(_, score, _, title, source, url, snippet), rank) =>
            Entry(
              title,
              WebsiteSourceType.toPublicString(source),
              url,
              snippet.toJSONString,
              QualityType.qualityFromScore(score, uris.size),
              rank
            )
        }

        // Bing entries inherit their quality attribute from the closest wikichimp entries
        val firstKnowQuality = entries.map(_.quality).filter {
          case QualityType.unknown => false
          case _ => true
        } match {
          case Nil => QualityType.unknown
          case head::tail => head
        }
        val emptyPe = List[Entry]()
        val init = (emptyPe, firstKnowQuality)
        val inherited = entries.foldLeft(init) {
          case ((acc, lastQuality), pe) =>
            pe.quality match {
              case QualityType.unknown => (acc:::List(pe.copy(quality = lastQuality)), lastQuality)
              case _ => (acc:::List(pe), pe.quality)
            }
        }._1

        // Create the public response
        Response(inherited, totalNb)
    }
  }
}
