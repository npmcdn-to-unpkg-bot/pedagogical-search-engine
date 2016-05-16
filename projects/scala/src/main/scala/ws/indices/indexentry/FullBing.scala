package ws.indices.indexentry

import java.sql.Timestamp

import org.json4s.DefaultFormats
import rsc.attributes.Source
import rsc.attributes.Source.Source
import ws.indices.bing.BingJsonProtocol.BingApiResult
import ws.indices.indexentry.EngineType.Engine
import ws.indices.snippet.{Line, Snippet}
import ws.indices.spraythings.SearchTerm

case class FullBing(entryId: String,
                    rank: Int,
                    title: String,
                    source: Source,
                    url: String,
                    snippet: Snippet,
                    timestamp: Timestamp)
extends IndexEntry with FullEntry {

  implicit val formats = DefaultFormats

  def slickEntryTuple(searchHash: Int)
  : mysql.slick.tables.Types.CacheEntry =
    (-1, searchHash, entryId, rank, "bing", source.toString)

  def slickDetailTuple()
  : mysql.slick.tables.Types.CacheDetail =
    (-1, entryId, title,
      url, org.json4s.native.Serialization.write(snippet), Some(timestamp))

  override def engine: Engine = EngineType.Bing
}

object FullBing {
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

      .replace("[Book]", "")

      .trim
  }

  def fromBingResult(result: BingApiResult, searchTerms: TraversableOnce[SearchTerm])
  : List[FullBing] = {
    // Create the full bing indices from the api results
    result.d.results.zipWithIndex.map {
      case (r, rank) =>
        val entryId = r.id
        val title = r.title
        val url = r.url
        val text = r.description
        val spots = Snippet.spotsFrom(text, searchTerms)
        val snippet = Snippet(Line(text, spots.toList)::Nil)

        FullBing(
          entryId,
          rank,
          beautifyBingTitle(title),
          Source.fromUrl(url),
          url,
          snippet,
          new Timestamp(new java.util.Date().getTime))
    }
  }
}

object FullBingMatch {
  def unapply(arg: FullBing): Option[Unit] = Some()
}
