package ws.exploration

import slick.jdbc.JdbcBackend._

object ExplorationAgent extends App {
  println("ay")

  val db = Database.forConfig("wikichimp.userStudy.slick")
}
