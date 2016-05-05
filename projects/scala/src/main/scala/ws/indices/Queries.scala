package ws.indices

import java.sql.Timestamp

import org.json4s.DefaultFormats
import slick.driver.MySQLDriver.api._
import slick.jdbc.{GetResult, PositionedParameters, PositionedResult, SetParameter}
import utils.StringUtils
import ws.indices.enums.EngineSourceType
import ws.indices.indexentry.{FullBing, IndexEntry, PartialBing, PartialWikichimp}

import scala.util.hashing.MurmurHash3


object Queries {
  private val cacheDetailsTQ = TableQuery[mysql.slick.tables.CacheDetails]
  private val cacheEntriesTQ = TableQuery[mysql.slick.tables.CacheEntries]
  private val searchesTQ = TableQuery[mysql.slick.tables.Searches]


  // Handle slick plain-sql "IN clause"
  implicit val setStringListParameter = new SetParameter[Set[String]]{
    def apply(v1: Set[String], v2: PositionedParameters): Unit = {
      v1.foreach(v2.setString)
    }
  }

  // Get the best indices (without the details)
  private object BestIndicesRConv
    extends GetResult[IndexEntry] {
    def apply(rs: PositionedResult) = {
      // Extract the attributes
      val source = rs.nextString()
      val entryId = rs.nextString()
      val scoreThing = rs.nextDouble()
      val resourceId = rs.nextString()

      // Match the type of the row
      source match {
        case _ if source.equals(EngineSourceType.Wikichimp.toString) =>
          PartialWikichimp(entryId, scoreThing, resourceId)
        case _ if source.equals(EngineSourceType.Bing.toString) =>
          PartialBing(entryId, scoreThing.toInt)
      }
    }
  }
  def bestIndices(unescapedUris: Set[String], nmax: Int) = {
    val searchHash: Int = MurmurHash3.unorderedHash(unescapedUris)
    val escapedUris = unescapedUris.map(uri => StringUtils.escapeSQLWildcards(uri))
    sql"""
(
  SELECT
    'wikichimp',
    EntryId,
    SUM(Score),
    MIN(ResourceId)
  FROM indices
  WHERE
    Uri IN ($escapedUris#${",?" * (escapedUris.size - 1)})
  GROUP BY entryId
  ORDER BY SUM(Score) DESC
  LIMIT 0, 500
) UNION (
  SELECT
    Source,
    EntryId,
    Rank,
    ''
  FROM `cache-entries`
    WHERE
    SearchHash = #$searchHash
)
    """.as[IndexEntry](BestIndicesRConv)
  }

  implicit val format = DefaultFormats

  def saveSearch(uris: Set[String], from: Int, to: Int) = {
    val searchHash: Int = MurmurHash3.unorderedHash(uris)
    val urisJson: String = org.json4s.native.Serialization.write(uris)

    DBIO.seq(
      searchesTQ += (-1, searchHash, urisJson, from, to, None)
    )
  }

  // Save some bing results (given the search "hash")
  def saveBingResult(uris: Set[String], bingEntries: List[FullBing]) = {
    val searchHash: Int = MurmurHash3.unorderedHash(uris)
    val entryRows = bingEntries.map(entry => entry.slickEntryTuple(searchHash))
    val detailRows = bingEntries.map(_.slickDetailTuple())

    DBIO.seq(
      cacheEntriesTQ ++= entryRows,
      cacheDetailsTQ ++= detailRows
    )
  }

  // Get details of some entries (from wikichimp, bing, etc..)
  def getDetails(unescapedEi: Set[String]) = {
    val escapedEi = unescapedEi.map(uri => StringUtils.escapeSQLWildcards(uri))
    sql"""
(
	SELECT
		EntryId,
		Title,
		TypeText,
		url,
		Snippet,
		Timestamp
	FROM `cache-details`
	WHERE EntryId IN ($escapedEi#${",?" * (escapedEi.size - 1)})
) UNION (
	SELECT
		EntryId,
		Title,
		Type,
		Href,
		Snippet,
    '2016-04-29 14:45:48'
	FROM details
    WHERE EntryId IN ($escapedEi#${",?" * (escapedEi.size - 1)})
);
    """.as[(String, String, String, String, String, Timestamp)]
  }
}

