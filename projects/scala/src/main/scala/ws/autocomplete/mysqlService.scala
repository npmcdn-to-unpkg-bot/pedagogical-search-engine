package ws.autocomplete

import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object mysqlService extends App {

  val db = Database.forConfig("wikichimp.autocomplete.slick")

  try {
      val text = "%hi"
      val test1 = Queries.fourPlus(text, 10)

    val future = db.run(test1.map(rs => {
      println(s"Completed: text=$text nbRes=${rs.size}")
    })).recover({
      case _ => {
        println(s"failed: $text")
      }
    })

    Await.result(future, Duration.Inf)
  } finally db.close
}
