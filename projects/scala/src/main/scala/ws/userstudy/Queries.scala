package ws.userstudy

import org.json4s.DefaultFormats
import slick.driver.MySQLDriver.api._
import ws.indices.spraythings.SearchTerm
import ws.userstudy.enum.ClassificationType.Classification


object Queries {
  private val clicksTQ = TableQuery[mysql.slick.tables.Clicks]
  private val classificationTQ = TableQuery[mysql.slick.tables.Classifications]
  private val messagesTQ = TableQuery[mysql.slick.tables.Messages]
  implicit val format = DefaultFormats

  def saveClick(searchTerms: List[SearchTerm], entryId: String,
                rank: Int, sid: Option[Int]) = {
    val searchHash: Int = SearchTerm.searchHash(searchTerms)

    DBIO.seq(
      clicksTQ += (-1, entryId, searchHash, sid, rank, None)
    )
  }

  def saveCl(searchTerms: List[SearchTerm], entryId: String,
             c: Classification, sid: Option[Int]) = {
    val searchHash: Int = SearchTerm.searchHash(searchTerms)

    DBIO.seq(
      classificationTQ += (-1, searchHash, entryId, sid, c.toString, None)
    )
  }

  def saveMessage(category: String, content: String,
                  sid: Option[Int]) = {
    DBIO.seq(
      messagesTQ += (-1, sid, category, content, None)
    )
  }
}

