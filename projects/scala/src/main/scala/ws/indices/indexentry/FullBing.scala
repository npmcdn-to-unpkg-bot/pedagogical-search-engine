package ws.indices.indexentry

import java.sql.Timestamp

import org.json4s.DefaultFormats
import ws.indices.bing.BingJsonProtocol.BingApiResult
import ws.indices.enums.WebsiteSourceType
import ws.indices.enums.WebsiteSourceType.WebsiteSource
import ws.indices.snippet.Snippet

case class FullBing(entryId: String,
                    rank: Int,
                    title: String,
                    source: WebsiteSource,
                    url: String,
                    snippet: Snippet,
                    timestamp: Timestamp)
extends IndexEntry with FullEntry {

  implicit val formats = DefaultFormats

  def slickEntryTuple(searchHash: Int)
  : mysql.slick.tables.Types.CacheEntry =
    (-1, searchHash, entryId, rank, "bing")

  def slickDetailTuple()
  : mysql.slick.tables.Types.CacheDetail =
    (-1, entryId, title, source.toString,
      url, org.json4s.native.Serialization.write(snippet), Some(timestamp))
}

object FullBing {
  def inferSource(url: String): WebsiteSource = {
    if(url.contains("coursera.org")) {
      WebsiteSourceType.Coursera
    } else if(url.contains("mit.edu")) {
      WebsiteSourceType.Mit
    } else if(url.contains("safaribooksonline.com")) {
      WebsiteSourceType.Safari
    } else if(url.contains("scholarpedia.org")) {
      WebsiteSourceType.Scholarpedia
    } else if(url.contains("khanacademy.org")) {
      WebsiteSourceType.Khan
    } else {
      WebsiteSourceType.Web
    }
  }

  def beautifyBingTitle(title: String): String = {
    title
      .replace("| Khan Academy", "")
      .replace("- Khan Academy", "")

      .replace("| Coursera", "")
      .replace("- Coursera", "")

      .replace("| MIT OpenCourseWare", "")
      .replace("- MIT OpenCourseWare", "")

      .replace("| Scholarpedia", "")
      .replace("- Scholarpedia", "")

      .replace("- Safari Books", "")
      .replace("- Safari", "")

      .trim
  }

  def fromBingResult(result: BingApiResult)
  : List[FullBing] = {
    // Create the full bing indices from the api results
    result.d.results.zipWithIndex.map {
      case (r, rank) =>
        val entryId = r.id
        val title = r.title
        val url = r.url
        val snippet = Snippet.fromText(r.description)

        FullBing(
          entryId,
          rank,
          beautifyBingTitle(title),
          inferSource(url),
          url,
          snippet,
          new Timestamp(new java.util.Date().getTime))
    }
  }
}

object FullBingMatch {
  def unapply(arg: FullBing): Option[Unit] = Some()
}
