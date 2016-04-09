package agents

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.common.settings.Settings

object BuildESAutocomplete {
  def main(args: Array[String]): Unit = {
    val settings = Settings.settingsBuilder()
      .put("http.enabled", false)
      .put("path.home", "/media/redwd/system/elasticsearch-2.3.0")
    val client = ElasticClient.local(settings.build)

    client.execute { index into "bands" / "artists" fields "name"->"coldplay" }.await

    Thread.sleep(2000)

    val resp = client.execute { search in "bands" / "artists" query "coldplay" }.await
    println(resp)

  }
}
