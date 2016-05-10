package ws.indices

import slick.jdbc.JdbcBackend.Database
import utils.{ListMixer, Logger}
import ws.indices.bing.BingFetcher
import ws.indices.bing.BingJsonProtocol.{BingApiResult, ResultElement, dElement}
import ws.indices.indexentry._
import ws.indices.spraythings.SearchTerm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.hashing.MurmurHash3

class IndicesFetcher(db: Database, bf: BingFetcher,
                     esFetcher: elasticsearch.IndicesFetcher) {

  def wcAndBing(searchTerms: TraversableOnce[SearchTerm], nmax: Int):
  Future[List[IndexEntry]] = {

    val uris: Set[String] = SearchTerm.uris(searchTerms).toSet
    val searchText = SearchTerm.searchText(searchTerms)
    val searchHash = SearchTerm.searchHash(searchTerms)

    // Get the indices from WC (based on the uris)
    // And the bing cached indices (based on the search hash)
    // Note: If the uris set is empty, only the bing cache is searched
    val dbAction = Queries.bestIndices(uris, searchHash, nmax)
    val dbFuture = db.run(dbAction).recover {
      case e =>
        Logger.error(s"Problem fetching the best indices(wc + bing-cache): ${e.getMessage}")
        Vector[IndexEntry]()
    }

    // Get the indices from WC full-text
    val esAction = esFetcher.getIndices(searchText, 0, 50)
    val esFuture = esAction.recover {
      case e =>
        Logger.error(s"Indices fetcher got an error with es-indices: ${e.getMessage}")
        Nil
    }
    val future = for {
      f1 <- dbFuture
      f2 <- esFuture
    } yield (f1, f2)

    future.flatMap {
      case (indexRows, wftIndices) =>
        // Classify the index rows
        val (partialBingRows, wcWithDup) = indexRows
          .foldLeft((List[PartialBing](), List[PartialWikichimp]())) {
            case ((bingAcc, wikichimpAcc), row) => row match {
              case r@PartialBing(_, _) => (bingAcc ::: List(r), wikichimpAcc)
              case r@PartialWikichimp(_, _, _) => (bingAcc, wikichimpAcc ::: List(r))
            }
          }

        // Remove the duplicates resources from Wikichimp
        val groupedWc = wcWithDup.groupBy(_.resourceId)
        val distinctWc = groupedWc.map {
          case (_, els) => els.sortBy(-_.sumScore).head
        }
        val sortedWc = distinctWc.toList.sortBy(-_.sumScore)

        // Remove the indices from elastic search that
        // already got returned by the wikichimp engine
        val wcEntryIds = sortedWc.map(_.entryId)
        val wft2 = wftIndices.filterNot(i => wcEntryIds.contains(i.entryId))

        // Create our results before mixing with other engines
        val ourIndices = ListMixer.mixWithPriority(sortedWc, wft2).toList

        // If there are no bing rows, go fetch them
        partialBingRows match {
          case Nil =>
            val futureApiResult = bf.search(searchText, 0, 10)

            // Remove duplicates based on titles
            futureApiResult.map(apiResult => {
              val accInit = (List[Set[Int]](), List[ResultElement]())

              apiResult.d.results.foldLeft(accInit) {
                case ((hashesSets, acc), result) =>
                  val text = result.title
                  val chunks = text.split(" ")
                    .filter(_.trim.length > 0).map(_.toLowerCase).toList
                  val hashes = chunks.map(MurmurHash3.stringHash)
                  val differences = hashesSets.map(set => hashes.filterNot(set.contains).length)
                  val minDiff = differences.size match {
                    case 0 => 0
                    case _ => differences.min
                  }

                  val threshold = math.ceil(chunks.size.toDouble * 0.25)

                  if(hashesSets.nonEmpty && minDiff <= threshold) {
                    (hashesSets, acc)
                  } else {
                    (hashes.toSet :: hashesSets, acc ::: List(result))
                  }
              }._2

            }).flatMap(result => {
              val fullBingResults = FullBing.fromBingResult(
                BingApiResult(dElement(result)), searchTerms
              )

              // Save them into the cache
              val action = Queries.saveBingResult(searchHash, fullBingResults)

              // Return them once they are cached
              db.run(action).map(_ => fullBingResults)
            }).map(results => {
              ListMixer.mixByFPreserveOrder(randomButConsistent, results, ourIndices).toList
            }).recover {
              // In case of bing-error, return only our results
              case e =>
                ourIndices
            }
          case _ =>
            // Otherwise, simply mix the results
            val mix = ListMixer.mixByFPreserveOrder(randomButConsistent, partialBingRows, ourIndices).toList
            Future.successful(mix)
        }
    }
  }

  private def randomButConsistent(entry: IndexEntry): String = {
    entry match {
      case PartialBing(entryId, _) => entryId
      case PartialWikichimp(entryId, _, _) => entryId
      case c@FullBingMatch(_) => c.entryId
      case x if x.isInstanceOf[FullWFT] => x.asInstanceOf[FullWFT].entryId
    }
  }
}
