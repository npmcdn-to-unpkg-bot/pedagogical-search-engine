package ws.exploration

import rsc.Formatters
import slick.lifted.TableQuery
import slick.driver.MySQLDriver.api._
import slick.jdbc.{GetResult, PositionedResult}
import ws.exploration.events.{Clicks, Messages, Searches}
import org.json4s.native.Serialization.read
import ws.exploration.attributes.Response
import ws.indices.SearchLog

object Queries extends Formatters {
  private val clicksTQ = TableQuery[mysql.slick.tables.Clicks]
  private val classificationTQ = TableQuery[mysql.slick.tables.Classifications]
  private val messagesTQ = TableQuery[mysql.slick.tables.Messages]

  // Convert a row into a click
  private object ClicksRConv
    extends GetResult[Clicks] {
    def apply(rs: PositionedResult) = {
      // Extract the attributes
      val autoId = rs.nextInt()
      val entryId = rs.nextString()
      val searchHash = rs.nextInt()
      val sid = rs.nextIntOption()
      val rank = rs.nextInt()
      val oTimestamp = rs.nextTimestampOption()

      Clicks(autoId, entryId, searchHash, sid, rank, oTimestamp)
    }
  }
  private object MessagesRConv
    extends GetResult[Messages] {
    def apply(rs: PositionedResult) = {
      // Extract the attributes
      val autoId = rs.nextInt()
      val sid = rs.nextIntOption()
      val category = rs.nextString()
      val content = rs.nextString()
      val oTimestamp = rs.nextTimestampOption()

      Messages(autoId, sid, category, content, oTimestamp)
    }
  }
  private object SearchesRConv
    extends GetResult[Searches] {
    def apply(rs: PositionedResult) = {
      // Extract the attributes
      val autoId = rs.nextInt()
      val searchHash = rs.nextInt()
      val sid = rs.nextIntOption()
      val searchLog = read[SearchLog](rs.nextString())
      val resultLog = read[Response](rs.nextString())
      val oTimestamp = rs.nextTimestampOption()

      Searches(autoId, searchHash, sid, searchLog, resultLog, oTimestamp)
    }
  }

  def allClicks() = {
    sql"""
      SELECT * FROM clicks;
    """.as[Clicks](ClicksRConv)
  }

  def allMessages() = {
    sql"""
      SELECT * FROM messages;
    """.as[Messages](MessagesRConv)
  }

  def allSearches() = {
    sql"""
      SELECT * FROM searches;
    """.as[Searches](SearchesRConv)
  }
}
