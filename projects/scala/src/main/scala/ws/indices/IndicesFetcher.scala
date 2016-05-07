package ws.indices

import slick.jdbc.JdbcBackend.Database
import utils.{ListMixer, Logger, StringUtils}
import ws.indices.bing.BingFetcher
import ws.indices.indexentry._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndicesFetcher(db: Database, bf: BingFetcher,
                     esFetcher: elasticsearch.IndicesFetcher) {

  def wcAndBing(uris: Set[String], nmax: Int):
  Future[List[IndexEntry]] = {

    val dbAction = Queries.bestIndices(uris, nmax)
    val searchText = uris.mkString(" ")
    val esAction = esFetcher.getIndices(searchText, 0, 50)

    val dbFuture = db.run(dbAction).recover {
      case e => {
        Logger.error(s"Indices fetcher got an error with db-indices: ${e.getMessage}")
        Vector[IndexEntry]()
      }
    }
    val esFuture = esAction.recover {
      case e => {
        Logger.error(s"Indices fetcher got an error with es-indices: ${e.getMessage}")
        Nil
      }
    }
    val future = for {
      f1 <- dbFuture
      f2 <- esFuture
    } yield (f1, f2)

    future.flatMap {
      case (indexRows, wftIndices) => {
        Logger.info(s"cache: ${indexRows.size}, full-text: ${wftIndices.size}")

        // Classify the index rows
        val (partialBingRows, wcWithDup) = indexRows
          .foldLeft((List[PartialBing](), List[PartialWikichimp]())) {
            case ((bingAcc, wikichimpAcc), row) => row match {
              case r@PartialBing(_, _) => (bingAcc ::: List(r), wikichimpAcc)
              case r@PartialWikichimp(_, _, _) => (bingAcc, wikichimpAcc ::: List(r))
            }
          }

        // Remove the duplicates resources from wikichimp
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
            val searchText = uris.map(StringUtils.labelizeUri(_)).mkString(" ")
            bf.search(searchText, 0, 10).flatMap(result => {
              val fullBingResults = FullBing.fromBingResult(result)

              // Save them into the cache
              val action = Queries.saveBingResult(uris, fullBingResults)

              // Return them once they are cached
              db.run(action).map(_ => fullBingResults)
            }).map(results => {
              ListMixer.mixByFPreserveOrder(randomButConsitent, results, ourIndices).toList
            }).recover {
              // In case of bing-error, return only our results
              case e =>
                ourIndices
            }
          case _ =>
            // Otherwise, simply mix the results
            val mix = ListMixer.mixByFPreserveOrder(randomButConsitent, partialBingRows, ourIndices).toList
            Future.successful(mix)
        }
      }
    }
  }

  private def randomButConsitent(entry: IndexEntry): String = {
    entry match {
      case PartialBing(entryId, _) => entryId
      case PartialWikichimp(entryId, _, _) => entryId
      case c@FullBingMatch(_) => c.entryId
      case x if x.isInstanceOf[FullWFT] => x.asInstanceOf[FullWFT].entryId
    }
  }
}
