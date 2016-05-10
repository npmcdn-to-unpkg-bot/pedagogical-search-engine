package elasticsearch

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import elasticsearch.parsers.Highlight
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.search.highlight.HighlightField
import ws.indices.enums.WebsiteSourceType
import ws.indices.indexentry.FullWFT
import ws.indices.snippet.Snippet

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class IndicesFetcher(esIndex: String,
                     esType: String,
                     ip: String = "127.0.0.1",
                     port: Int = 9300,
                     clusterName: String = "elasticsearch") {
  // Private things
  private val uri = ElasticsearchClientUri(s"elasticsearch://$ip:$port")
  private val settings = Settings.settingsBuilder()
    .put("cluster.name", clusterName)
  private val client = ElasticClient.transport(settings.build(), uri)

  private val bodyField = "body"
  private val titleField = "title"
  private val sourceField = "source"
  private val entryIdField = "entryId"
  private val urlField = "href"
  private val rIdField = "resourceId"

  // Public methods
  def getIndices(searchText: String, from: Int, to: Int)
  : Future[List[FullWFT]] = {

    // Build the query
    val queryCore =
      search in
        esIndex / esType query {
        bool {
          should(
            matchQuery(titleField, searchText),
            matchQuery(bodyField, searchText)
          )
        }
      } highlighting {
        highlight(bodyField)
      } fields(titleField, sourceField, entryIdField, urlField, rIdField)

    val query = queryCore start from limit (to - from + 1)

    // Execute & create the indices
    val executed = Try { client.execute(query) }
    val future = executed match {
      case Success(x) => x
      case Failure(e) => Future.failed(e)
    }

    future.map(rsp => {
      val hits = rsp.hits.toList

      hits.map(hit => {
        val score = hit.score
        val title = hit.field(titleField).getValue[String]
        val source = hit.field(sourceField).getValue[String]
        val entryId = hit.field(entryIdField).getValue[String]
        val url = hit.field(urlField).getValue[String]
        val resourceId = hit.field(rIdField).getValue[String]
        val snippet = hit.highlightFields.contains(bodyField) match {
          case true => toSnippet(hit.highlightFields(bodyField))
          case false => Snippet(Nil)
        }

        FullWFT(entryId, score, resourceId, title, WebsiteSourceType.withName(source),
          url, snippet)
      })
    })
  }

  private def toSnippet(h: HighlightField)
  : Snippet = {
    val fragments = h.fragments()
    val lines = fragments.flatMap(f => {
      val fragment = f.string()
      val chunks = fragment.split("\n")

      // Produce the line
      chunks.map(chunk => Highlight.parse(chunk))
    })

    // Sort by number of spots desc
    val sorted = lines.sortBy(-_.spots.size)

    Snippet(sorted.take(3).toList)
  }
}
