package ws.indices


import org.json4s.native.Serialization.write
import slick.jdbc.JdbcBackend._
import utils.{Logger, Settings}
import ws.indices.bing.BingFetcher
import ws.indices.indexentry.{EngineType, FullBing, FullWFT, FullWikichimp}
import ws.indices.response.{Entry, NbResults, QualityType, Response}
import ws.indices.spraythings.FilterParameterType.FilterParameter
import ws.indices.spraythings.{FilterParameterType, Search, SearchTerm}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SearchExecutor(settings: Settings) extends rsc.Formatters {

  // Create the service itself
  private val db = Database.forConfig("wikichimp.indices.ws.slick")

  private val bingFetcher = new BingFetcher(timeoutMs = 5000)

  private val esIndicesFetcher = new elasticsearch.IndicesFetcher(
    settings.ElasticSearch.esIndex,
    settings.ElasticSearch.esType,
    settings.ElasticSearch.ip,
    settings.ElasticSearch.javaApiPort
  )
  private val indicesFetcher = new IndicesFetcher(db, bingFetcher, esIndicesFetcher)
  private val detailsFetcher = new DetailsFetcher(db)

  // Some constants
  private val N_MAX = 500

  // Define the search-strategy
  def search(search: Search)
  : Future[Response] = {
    // defaults
    val from = search.from.getOrElse(0)
    val to = search.to.getOrElse(from + 9)
    val distance = to - from + 1

    // First validations: discard huge inputs
    val rejectResponse = Future.successful(Response(Nil, NbResults(0, 0, 0)))
    if(search.searchTerms.size > 10 ||
      from > to || from < 0 || to >= N_MAX ||
      distance < 1 || distance > 10) {
      rejectResponse

    } else {
      // The input sounds reasonable but perform a in-depth validation
      val searchTerms = SearchTerm.validationSkim(search.searchTerms).toList
      if(searchTerms.isEmpty) {
        rejectResponse
      } else {
        unvalidatedSearch(searchTerms, from, to,
          search.filter.getOrElse(FilterParameterType.All), search.sid)
      }
    }
  }

  def unvalidatedSearch(searchTerms: List[SearchTerm],
                        from: Int,
                        to: Int,
                        filter: FilterParameter,
                        sid: Option[Int])
  : Future[Response] = {
    // Fetch all the best indices (without their details yet)
    val searchFuture = indicesFetcher.wcAndBing(searchTerms, N_MAX, filter).flatMap {
      case (indexEntries, nbResults) =>
        // fetch the details in the [from, to] interval
        val interval = indexEntries.slice(from, to + 1)
        detailsFetcher.resolve(interval, searchTerms).map(r => (r, nbResults))

    }.map {
      case (indices, totalNb) =>
        // create the public entries
        val entries: List[response.Entry] = indices.zipWithIndex.map {
          case (c@FullBing(entryId, _, title, source, url, snippet, _), rank) =>
            Entry(
              entryId,
              title,
              source.toString,
              url,
              snippet.toJSONString,
              QualityType.unknown,
              rank,
              EngineType.Bing,
              None
            )

          case (c@FullWikichimp(entryId, score, _, title, source, url, snippet, topIndices), rank) =>
            Entry(
              entryId,
              title,
              source.toString,
              url,
              snippet.toJSONString,
              QualityType.qualityFromScore(score, SearchTerm.uris(searchTerms).size),
              rank,
              EngineType.Wikichimp,
              Some(topIndices.map(_.uri))
            )

          case (c@FullWFT(entryId, score, resourceId, title, source, url, snippet, topIndices), rank) =>
            Entry(
              entryId,
              title,
              source.toString,
              url,
              snippet.toJSONString,
              QualityType.unknown,
              rank,
              EngineType.WikichimpFT,
              Some(topIndices.map(_.uri))
            )
        }

        // Unknown quality types are infered from the closest wikichimp entries
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

    // No sid, no logging and hence purely anonymous search
    sid match {
      case None => searchFuture
      case Some(_) =>
        searchFuture.flatMap {
          case response =>
            // Log the search
            val searchLog = write(SearchLog(from, to, filter, searchTerms))
            val resultLog = write(response)
            val logAction = Queries.saveSearch(searchTerms, sid,
              searchLog, resultLog)

            // Return anyway the response
            db.run(logAction).map {
              case _ => response
            }.recover {
              case e =>
                // We do not really care about failures
                Logger.stackTrace("there was a log failure", e)
                response
            }
        }
    }
  }
}
