package ws.indices

import ws.indices.indexentry._
import slick.jdbc.JdbcBackend.Database
import ws.indices.enums.WebsiteSourceType
import ws.indices.snippet.Snippet

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DetailsFetcher(db: Database) {

  def resolve(entries: List[IndexEntry], uris: Set[String])
  : Future[List[FullEntry]] = {
    // Split entries by type
    val emptyAccFB = List[(Int, FullBing)]()
    val emptyAccFW = List[(Int, FullWikichimp)]()
    val emptyAccPB = List[(Int, PartialBing)]()
    val emptyAccPW = List[(Int, PartialWikichimp)]()
    val emptyAcc = (emptyAccFB, emptyAccFW, emptyAccPB, emptyAccPW)

    val (fbs, fws, pbs, pws) = entries.zipWithIndex.foldLeft(emptyAcc) {
      case ((fb, fw, pb, pw), (entry, index)) => entry match {
        case c@FullBingMatch(_) =>
          ((index, c)::fb, fw, pb, pw)
        case c@FullWikichimp(_,_,_,_,_,_,_) =>
          (fb, (index, c)::fw, pb, pw)
        case c@PartialBing(_,_) =>
          (fb, fw, (index, c)::pb, pw)
        case c@PartialWikichimp(_,_,_) =>
          (fb, fw, pb, (index, c)::pw)
      }
    }

    // Is there any partial entries to complete?
    pbs.map(_._2.entryId):::pws.map(_._2.entryId) match {
      case Nil =>
        Future.successful((fbs:::fws).sortBy(_._1).map(_._2))

      case entryIds =>
        // Get details of the partial entries
        val action = Queries.getDetails(entryIds.toSet)
        db.run(action).map(detailRows => {
          val details = detailRows.groupBy(_._1).map(g => (g._1, g._2.head))

          // Complete the partial entries
          val completedBing = pbs.map {
            case (rank, PartialBing(entryId, _)) =>
              val d = details(entryId)

              val title = d._2
              val url = d._4
              val source = FullBing.inferSource(url)
              val snippet = Snippet.fromText(d._5)
              val timestamp = d._6
              (rank, FullBing(entryId, rank, title, source, url, snippet, timestamp))
          }

          val completedWikichimp = pws.map {
            case (rank, PartialWikichimp(entryId, sumScore, resourceId)) =>
              val d = details(entryId)

              val title = d._2
              val url = d._4
              val source = WebsiteSourceType.fromWcTypeField(d._3)
              val rscSnippet = rsc.snippets.Snippet.fromJSONString(d._5)
              val snippet = Snippet.fromRscSnippet(rscSnippet, uris)

              (rank, FullWikichimp(entryId, sumScore, resourceId, title, source, url, snippet))
          }

          // Return the completed entries
          val all = fbs:::fws:::completedBing:::completedWikichimp
          val sorted = all.sortBy(_._1)
          sorted.map(_._2)
        })
    }
  }
}
