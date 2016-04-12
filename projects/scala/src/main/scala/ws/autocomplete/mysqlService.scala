package ws.autocomplete

import slick.jdbc.JdbcBackend.Database
import ws.autocomplete.results._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object mysqlService extends App {

  val db = Database.forConfig("wikichimp.autocomplete.slick")

  try {
    val text = "fst"
    val nbResults = 10

    def projectSize(s: String): Int = math.min(s.size, text.size + 3)
    def contains(acc: List[Result], uri: String): Boolean = acc match {
      case Nil => false
      case head::tail => (head match {
        case Disambiguation(uri2, _, _) => uri2
        case Redirect(_, _, uri2, _) => uri2
        case Title(_, uri2, _) => uri2
      }).equals(uri) || contains(tail, uri)
    }

    // Create the query-action
    val action = text.size match {
      case one if one == 1 => ???
      case twoThre if (twoThre == 2 || twoThre == 3) => Queries.twoThre(text, nbResults)
      case fourPlus => Queries.fourPlus(text, nbResults)
    }

    val future = db.run(action.map(results => {
      val ranked = results.groupBy(r => {
        val label = r match  {
          case Disambiguation(_, label, _) => label
          case Redirect(label, _, _, _) => label
          case Title(label, _, _) => label
        }
        projectSize(label) // smallest lengths first
      }).toList.sortBy {
        case (length, _) => length
      }.flatMap {
        case (l, rs) => rs.sortBy {
          case Disambiguation(_, _, _) => Integer.MIN_VALUE // Disambiguations first
          case Redirect(_, _, _, in) => -in // max |In| then
          case Title(_, _, in) => -in
        }
      }.foldLeft(List[Result]()) { // Filter out duplicate "uri"s
        case (acc, result) => (result match {
          case Disambiguation(uriA, _, _) => contains(acc, uriA)
          case Redirect(_, _, uriB, _) => contains(acc, uriB)
          case Title(_, uri, _) => contains(acc, uri)
        }) match {
          case true => acc
          case false => acc:::List(result)
        }
      }.take(nbResults)


      println(s"Completed: nbRes=${results.size}, ranking:")
      ranked.map(r => r match {
        case Disambiguation(uriA, labelA, bs) => {
          println(s"d: $labelA($uriA)")
          bs.map {
            case PageElement(uri, label, in) =>
              println(s"   $label ($uri: $in)")
          }
        }
        case Redirect(labelA, labelB, uriB, inB) => {
          println(s"r: $labelA ($uriB: $inB)")
        }
        case Title(label, uri, in) => {
          println(s"t: $label ($uri: $in)")
        }
      })
    })).recover({
      case e => {
        println(s"failed: $text, reason: ${e.getMessage}")
        e.printStackTrace()
      }
    })

    Await.result(future, Duration.Inf)
  } finally db.close
}
