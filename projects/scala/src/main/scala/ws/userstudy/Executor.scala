package ws.userstudy

import slick.jdbc.JdbcBackend._
import utils.StringUtils
import ws.indices.response.QualityType
import ws.indices.spraythings.FilterParameterType
import ws.userstudy.enum.ClassificationType
import ws.userstudy.spraythings.{ClassificationInput, ClickInput}

import scala.concurrent.Future

class Executor {

  // Create the service itself
  private val db = Database.forConfig("wikichimp.userStudy.slick")

  // Save a user click
  def saveClick(clickInput: ClickInput)
  : Future[Unit] = {
    // Validate the input
    val filter = FilterParameterType.withName(clickInput.filter)
    if(clickInput.entryId.length > 36) {
      Future.failed(new Exception("EntryId is too long (>36)"))
    } else if(clickInput.rank < 0) {
      Future.failed(new Exception("Rank is less than o"))
    } else {
      val quality = QualityType.fromString(clickInput.quality)
      val uris = clickInput.uris.map(uri => StringUtils.normalizeUri(uri)).toSet

      // Save the click
      val action = Queries.saveClick(
        uris,
        clickInput.entryId,
        clickInput.rank,
        quality,
        filter
      )
      db.run(action)
    }
  }

  // Save a user classification
  def saveCl(ci: ClassificationInput)
  : Future[Unit] = {
    // Validate the input
    val filter = FilterParameterType.withName(ci.filter)
    if(ci.entryId.length > 36) {
      Future.failed(new Exception("EntryId is too long (>36)"))
    } else {
      val cls = ClassificationType.withName(ci.classification)
      val uris = ci.uris.map(uri => StringUtils.normalizeUri(uri)).toSet
      val action = Queries.saveCl(uris, ci.entryId, cls, filter)
      db.run(action)
    }
  }
}
