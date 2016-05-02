package ws.indices

import slick.jdbc.JdbcBackend.Database
import utils.{ListMixer, StringUtils}
import ws.indices.bing.BingFetcher
import ws.indices.indexentry._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndicesFetcher(db: Database, bf: BingFetcher) {

  def randomButConsitent(entry: IndexEntry): String = {
    entry match {
      case PartialBing(entryId, _) => entryId
      case PartialWikichimp(entryId, _, _) => entryId
      case c@FullBingMatch(_) => c.entryId
    }
  }

  def wcAndBing(uris: Set[String], nmax: Int):
  Future[List[IndexEntry]] = {
    val action = Queries.bestIndices(uris, nmax)

    db.run(action).flatMap(indexRows => {
      // Classify the rows
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
            ListMixer.mixByFPreserveOrder(randomButConsitent, results, sortedWc).toList
          }).recover {
            // In case of bing-error, return only our results
            case e =>
              sortedWc
          }
        case _ =>
          // Otherwise, simply mix the results
          val mix = ListMixer.mixByFPreserveOrder(randomButConsitent, partialBingRows, sortedWc).toList
          Future.successful(mix)
      }
    })
  }
}
