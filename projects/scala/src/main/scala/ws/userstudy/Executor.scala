package ws.userstudy

import slick.jdbc.JdbcBackend._
import ws.indices.spraythings.SearchTerm
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
    if(clickInput.entryId.length > 36) {
      Future.failed(new Exception("EntryId is too long (>36)"))

    } else if(clickInput.rank < 0) {
      Future.failed(new Exception("Rank is less than o"))

    } else {
      val searchTerms = SearchTerm.validationSkim(clickInput.searchTerms).toList

      searchTerms.nonEmpty match {
        case true =>
          // Save the click
          val action = Queries.saveClick(
            searchTerms,
            clickInput.entryId,
            clickInput.rank,
            clickInput.sid
          )
          db.run(action)

        case false =>
          Future.failed(new Exception("No search terms"))
      }
    }
  }

  // Save a user classification
  def saveCl(ci: ClassificationInput)
  : Future[Unit] = {
    // Validate the input
    if(ci.entryId.length > 36) {
      Future.failed(new Exception("EntryId is too long (>36)"))

    } else {
      val searchTerms = SearchTerm.validationSkim(ci.searchTerms).toList

      searchTerms.isEmpty match {
        case true =>
          Future.failed(new Exception("No search terms"))

        case false =>
          try {
            val cls = ClassificationType.withName(ci.classification)
            val action = Queries.saveCl(searchTerms, ci.entryId, cls, ci.sid)
            db.run(action)
          } catch {
            case e: Throwable =>
              Future.failed(e)
          }
      }
    }
  }
}
