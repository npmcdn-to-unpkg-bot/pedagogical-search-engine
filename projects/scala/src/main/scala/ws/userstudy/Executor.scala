package ws.userstudy

import slick.jdbc.JdbcBackend._

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
}
