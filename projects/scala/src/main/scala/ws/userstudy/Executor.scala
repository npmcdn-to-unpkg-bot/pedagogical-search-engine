package ws.userstudy

import slick.jdbc.JdbcBackend._
import ws.indices.response.QualityType
import ws.userstudy.enum.ClassificationType
import ws.userstudy.spraythings.{ClassificationInput, ClickInput}

import scala.concurrent.Future

class Executor {

  // Create the service itself
  private val db = Database.forConfig("wikichimp.userStudy.slick")

  // Save a user search
  def saveSearch(urisList: List[String])
  : Future[Unit] = {
    // Validate the input
    val uris = urisList.toSet

    // Save the search
    val action = Queries.saveSearch(uris)
    db.run(action)
  }

  // Save a user click
  def saveClick(clickInput: ClickInput)
  : Future[Unit] = {
    // Validate the input
    if(clickInput.entryId.size > 36) {
      Future.failed(new Exception("EntryId is too long (>36)"))
    } else if(clickInput.rank < 0) {
      Future.failed(new Exception("Rank is less than o"))
    } else {
      val quality = QualityType.fromString(clickInput.quality)
      val uris = clickInput.uris.toSet

      // Save the click
      val action = Queries.saveClick(
        uris,
        clickInput.entryId,
        clickInput.rank,
        quality
      )
      db.run(action)
    }
  }

  // Save a user classification
  def saveCl(ci: ClassificationInput)
  : Future[Unit] = {
    // Validate the input
    if(ci.entryId.size > 36) {
      Future.failed(new Exception("EntryId is too long (>36)"))
    } else {
      ClassificationType.fromString(ci.classification) match {
        case None => Future.failed(new Exception(s"Unkown classification: ${ci.classification}"))
        case Some(cl) =>
          val uris = ci.uris.toSet
          val action = Queries.saveCl(uris, ci.entryId, cl)
          db.run(action)
      }
    }
  }
}
