package ws.indices

import java.sql.Timestamp

import slick.jdbc.JdbcBackend.Database
import ws.indices.indexentry._
import ws.indices.snippet.Snippet
import ws.indices.spraythings.SearchTerm
import org.json4s.native.Serialization.read

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DetailsFetcher(db: Database) extends Formatters {

  def resolve(entries: List[IndexEntry], searchTerms: List[SearchTerm])
  : Future[List[FullEntry]] = {

    // Get partial entries
    val emptyAccPB = List[PartialBing]()
    val emptyAccPW = List[PartialWikichimp]()
    val emptyAcc = (emptyAccPB, emptyAccPW)

    val (partialBings, partialWcs) =
      entries.foldLeft(emptyAcc) {
        case ((accPB, accPW), entry) => entry match {

          case c@PartialBing(_, _, _) =>
            (c::accPB, accPW)

          case c@PartialWikichimp(_, _, _, _) =>
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

          case PartialBing(entryId, rank, source) =>
            val d = detailsMap(entryId)

            val title = d._2
            val url = d._3
            val snippet = Snippet.fromSnippetJSON(d._4)
            val timestamp = d._5
            FullBing(entryId, rank, title, source, url, snippet, timestamp)

          case PartialWikichimp(entryId, sumScore, resourceId, source) =>
            val d = detailsMap(entryId)

            val title = d._2
            val url = d._3
            val rscSnippet = rsc.snippets.Snippet.fromJSONString(d._4)
            val uris = SearchTerm.uris(searchTerms)
            val snippet = Snippet.fromRscSnippet(rscSnippet, uris.toSet)
            val TopIndicesJson = read[List[rsc.indexers.Index]](d._6)

            FullWikichimp(entryId, sumScore, resourceId, title, source, url, snippet, TopIndicesJson)
        }
      })
    }
  }

  type row = (String, String, String, String, Timestamp, String)

  def getDetails(entryIds: Set[String])
  : Future[Map[String, (row)]] = {
    val action = Queries.getDetails(entryIds)
    db.run(action).map(rows => {
      rows.map(row => (row._1, row)).toMap
    })
  }
}
