package ws.indices

import java.sql.Timestamp

import slick.jdbc.JdbcBackend.Database
import ws.indices.enums.WebsiteSourceType
import ws.indices.indexentry._
import ws.indices.snippet.Snippet
import ws.indices.spraythings.SearchTerm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DetailsFetcher(db: Database) {

  def resolve(entries: List[IndexEntry], searchTerms: List[SearchTerm])
  : Future[List[FullEntry]] = {

    // Get partial entries
    val emptyAccPB = List[PartialBing]()
    val emptyAccPW = List[PartialWikichimp]()
    val emptyAcc = (emptyAccPB, emptyAccPW)

    val (partialBings, partialWcs) =
      entries.foldLeft(emptyAcc) {
        case ((accPB, accPW), entry) => entry match {

          case c@PartialBing(_,_) =>
            (c::accPB, accPW)

          case c@PartialWikichimp(_,_,_) =>
            (accPB, c::accPW)

          case _ =>
            (accPB, accPW)
        }
      }

    // Is there any partial entries to complete?
    val entryIds = partialBings.map(_.entryId):::partialWcs.map(_.entryId)
    entryIds match {
      case Nil => Future.successful(entries.asInstanceOf[List[FullEntry]])
      case _ => getDetails(entryIds.toSet).map(detailsMap => {
        // Complete the partial entries
        entries.map {
          case entry if entry.isInstanceOf[FullEntry] =>
            entry.asInstanceOf[FullEntry]

          case PartialBing(entryId, rank) =>
            val d = detailsMap(entryId)

            val title = d._2
            val url = d._4
            val source = FullBing.inferSource(url)
            val snippet = Snippet.fromSnippetJSON(d._5)
            val timestamp = d._6
            FullBing(entryId, rank, title, source, url, snippet, timestamp)

          case PartialWikichimp(entryId, sumScore, resourceId) =>
            val d = detailsMap(entryId)

            val title = d._2
            val url = d._4
            val source = WebsiteSourceType.fromWcTypeField(d._3)
            val rscSnippet = rsc.snippets.Snippet.fromJSONString(d._5)
            val uris = SearchTerm.uris(searchTerms)
            val snippet = Snippet.fromRscSnippet(rscSnippet, uris.toSet)

            FullWikichimp(entryId, sumScore, resourceId, title, source, url, snippet)
        }
      })
    }
  }

  type row = (String, String, String, String, String, Timestamp)

  def getDetails(entryIds: Set[String])
  : Future[Map[String, (row)]] = {
    val action = Queries.getDetails(entryIds)
    db.run(action).map(rows => {
      rows.map(row => (row._1, row)).toMap
    })
  }
}
