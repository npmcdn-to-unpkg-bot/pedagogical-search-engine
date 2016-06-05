package ws.indices

import slick.jdbc.JdbcBackend.Database
import utils.{ListMixer, Logger}
import ws.indices.bing.BingFetcher
import ws.indices.bing.BingJsonProtocol.{BingApiResult, ResultElement, dElement}
import ws.indices.indexentry._
import ws.indices.response.NbResults
import ws.indices.spraythings.FilterParameterType.FilterParameter
import ws.indices.spraythings.{FilterParameterType, SearchTerm}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.hashing.MurmurHash3

class IndicesFetcher(db: Database, bf: BingFetcher,
                     esFetcher: elasticsearch.IndicesFetcher) {

  def wcAndBing(searchTerms: TraversableOnce[SearchTerm],
                nmax: Int,
                filter: FilterParameter):
  Future[(List[IndexEntry], NbResults)] = {

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

    // Get the indices from WC full-text [disabled]
    val esFuture = Future.successful(List[FullWFT]())
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
              case r@PartialBing(_, _, _) => (bingAcc ::: List(r), wikichimpAcc)
              case r@PartialWikichimp(_, _, _, _) => (bingAcc, wikichimpAcc ::: List(r))
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

        // Filter the results according to the filter parameter
        val filteredWft = filterEntries(wft2, filter)
        val filteredWc = filterEntries(sortedWc, filter)

        // Produce some statistics
        val wftAllSize = filterEntries(wft2, FilterParameterType.All).size
        val wftFreeSize = filterEntries(wft2, FilterParameterType.Free).size
        val wftPaidSize = filterEntries(wft2, FilterParameterType.Paid).size

        val wcAllSize = filterEntries(sortedWc, FilterParameterType.All).size
        val wcFreeSize = filterEntries(sortedWc, FilterParameterType.Free).size
        val wcPaidSize = filterEntries(sortedWc, FilterParameterType.Paid).size

        // Create our results before mixing with other engines
        val ourIndices = filteredWc ::: filteredWft

        // If there are no bing rows, go fetch them
        partialBingRows match {
          case Nil =>
            val futureApiResult: Future[BingApiResult] = bf.search(searchText, 0, 10)

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
              // Filter the results according to the filter parameter
              val filtered = filterEntries(results, filter)

              // Count the results
              val allResultsSize = filterEntries(results, FilterParameterType.All).size
              val freeResultsSize = filterEntries(results, FilterParameterType.Free).size
              val paidResultsSize = filterEntries(results, FilterParameterType.Paid).size

              // Produce some statistics
              val all = wcAllSize + wftAllSize + allResultsSize
              val free = wcFreeSize + wftFreeSize + freeResultsSize
              val paid = wcPaidSize + wftPaidSize + paidResultsSize

              val list = ListMixer.mixByFPreserveOrder(randomButConsistent, filtered, ourIndices).toList
              (list, NbResults(all, free, paid))
            }).recover {
              // In case of bing-error, return only our results
              case e =>
                // Produce some statistics
                val all = wcAllSize + wftAllSize
                val free = wcFreeSize + wftFreeSize
                val paid = wcPaidSize + wftPaidSize

                (ourIndices, NbResults(all, free, paid))
            }

          case _ =>
            // Count the results
            val allPbrSize = filterEntries(partialBingRows, FilterParameterType.All).size
            val freePbrSize = filterEntries(partialBingRows, FilterParameterType.Free).size
            val paidPbrSize = filterEntries(partialBingRows, FilterParameterType.Paid).size

            val all = wcAllSize + wftAllSize + allPbrSize
            val free = wcFreeSize + wftFreeSize + freePbrSize
            val paid = wcPaidSize + wftPaidSize + paidPbrSize

            // Otherwise, simply mix the results
            val filteredBR = filterEntries(partialBingRows, filter)
            val mix = ListMixer.mixByFPreserveOrder(randomButConsistent, filteredBR, ourIndices).toList
            Future.successful(mix, NbResults(all, free, paid))
        }
    }
  }

  private def filterEntries[T <: IndexEntry](entries: List[T], filter: FilterParameter)
  : List[T] = entries.filter {
    case x => FilterParameterType.isSourceAllowed(filter, x.source)
  }

  private def randomButConsistent(entry: IndexEntry): String = {
    entry match {
      case PartialBing(entryId, _, _) => entryId
      case PartialWikichimp(entryId, _, _, _) => entryId
      case c@FullBingMatch(_) => c.entryId
      case x if x.isInstanceOf[FullWFT] => x.asInstanceOf[FullWFT].entryId
    }
  }
}
