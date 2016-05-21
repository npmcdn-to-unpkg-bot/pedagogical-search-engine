package ws.indices

import java.sql.Timestamp

import org.json4s.DefaultFormats
import rsc.attributes.Source
import slick.driver.MySQLDriver.api._
import slick.jdbc.{GetResult, PositionedParameters, PositionedResult, SetParameter}
import ws.indices.enums.EngineSourceType
import ws.indices.indexentry.{FullBing, IndexEntry, PartialBing, PartialWikichimp}
import ws.indices.spraythings.SearchTerm


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
      val typeText = Source.fromString(rs.nextString())

      // Match the type of the row
      source match {
        case _ if source.equals(EngineSourceType.Wikichimp.toString) =>
          PartialWikichimp(entryId, scoreThing, resourceId, typeText)

        case _ if source.equals(EngineSourceType.Bing.toString) =>
          PartialBing(entryId, scoreThing.toInt, typeText)
      }
    }
  }

  def bestIndices(uris: Set[String], searchHash: Int, nmax: Int) = {
    if(uris.isEmpty) {
      bestIndicesBingOnly(searchHash)
    } else {
      bestIndicesBingAndWC(uris, searchHash, nmax)
    }
  }

  def bestIndicesBingOnly(searchHash: Int) = {
    sql"""
(
  SELECT
    Source,
    EntryId,
    Rank,
    '',
    TypeText
  FROM `cache-entries`
    WHERE
    SearchHash = #$searchHash
)
    """.as[IndexEntry](BestIndicesRConv)
  }

  def bestIndicesBingAndWC(uris: Set[String], searchHash: Int, nmax: Int) = {
    sql"""
(
  SELECT
    'wikichimp',
    EntryId,
    SUM(Score),
    MIN(ResourceId),
    MIN(TypeText)
  FROM `indices`
  WHERE
    Uri IN ($uris#${",?" * (uris.size - 1)})
  GROUP BY entryId
  ORDER BY SUM(Score) DESC
  LIMIT 0, #$nmax
) UNION (
  SELECT
    Source,
    EntryId,
    Rank,
    '',
    TypeText
  FROM `cache-entries`
    WHERE
    SearchHash = #$searchHash
)
    """.as[IndexEntry](BestIndicesRConv)
  }

  implicit val format = DefaultFormats

  def saveSearch(searchTerms: List[SearchTerm], sid: Option[Int],
                 searchLog: String, resultLog: String) = {
    val searchHash: Int = SearchTerm.searchHash(searchTerms)
    val jsonLog: String = org.json4s.native.Serialization.write(searchTerms)

    DBIO.seq(
      searchesTQ += (-1, searchHash, sid, searchLog, resultLog, None)
    )
  }

  // Save some bing results (given the search "hash")
  def saveBingResult(searchHash: Int, bingEntries: List[FullBing]) = {
    val entryRows = bingEntries.map(entry => entry.slickEntryTuple(searchHash))
    val detailRows = bingEntries.map(_.slickDetailTuple())

    DBIO.seq(
      cacheEntriesTQ ++= entryRows,
      cacheDetailsTQ ++= detailRows
    )
  }

  // Get details of some entries (from wikichimp, bing, etc..)
  def getDetails(uris: Set[String]) = {
    sql"""
(
	SELECT
		EntryId,
		Title,
		url,
		Snippet,
		Timestamp,
		''
	FROM `cache-details`
	WHERE EntryId IN ($uris#${",?" * (uris.size - 1)})
) UNION (
	SELECT
		EntryId,
		Title,
		Href,
		Snippet,
    '2016-04-29 14:45:48',
    TopIndicesJson
	FROM `details`
    WHERE EntryId IN ($uris#${",?" * (uris.size - 1)})
);
    """.as[(String, String, String, String, Timestamp, String)]
  }
}

