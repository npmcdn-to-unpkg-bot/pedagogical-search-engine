package ws.indices

import mysql.slick.tables.{Indices, Details}
import slick.driver.MySQLDriver.api._

object Queries {
  val indicesTQ = TableQuery[Indices]
  val detailsTQ = TableQuery[Details]

  def paged(uris: Set[String], from: Int = 0, to: Int = 9) = {
    // Prepare the statement
    val matchedIndices = for {
      i <- indicesTQ if i.uri inSetBind uris
    } yield i
    val sumed = matchedIndices.groupBy(_.entryId).map {
      case (entryId, group) => (entryId, group.map(_.score).sum)
    }
    val quantity = to - from + 1
    val skimmed = sumed.sortBy(p => p._2.desc).drop(from).take(quantity)

    // Return an "slick-action"
    skimmed.result
  }

  def details(entryIds: Seq[String]) = {
    // Prepare the statement
    val q = for {
      d <-detailsTQ if d.entryId inSetBind entryIds
    } yield (d.entryId, d.title, d.typeCol, d.href, d.snippet, d.resourceId)

    // Return an "slick-action"
    q.result
  }
}

