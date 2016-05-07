package elasticsearch

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import org.elasticsearch.common.settings.Settings
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class IndicesFetcher(esIndex: String,
                     esType: String,
                     ip: String = "127.0.0.1",
                     port: Int = 9300,
                     clusterName: String = "elasticsearch") {

  def getIndices(searchText: String)
  : Future[Any] = Future {
    val uri = ElasticsearchClientUri(s"elasticsearch://$ip:$port")
    val settings = Settings.settingsBuilder()
      .put("cluster.name", clusterName)

    val client = ElasticClient.transport(settings.build(), uri)

    val query =
      search in
        esIndex / esType query {
        bool {
          should(
            matchQuery("title", searchText),
            matchQuery("body", searchText)
          )
        }
      } highlighting {
        highlight("body")
      } fields("title", "source", "entryId", "href", "resourceId")

    val resp = client.execute {
      query start 0 limit 10
    }.await
    println(resp)
  }
}
