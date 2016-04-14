package ws.autocomplete.spraythings

import org.json4s._
import org.json4s.native.Serialization.write
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.HttpService
import spray.http.MediaTypes.`application/json`
import ws.autocomplete.MysqlService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

case class Search(text: String)

object SearchJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PersonFormat = jsonFormat1(Search)
}

trait Service extends HttpService {

  val mysqlService = new MysqlService()
  import SearchJsonSupport._
  implicit val formats = DefaultFormats

  val myRoute =
    path("autocomplete") {
      post {
        entity(as[Search]) { search =>
          respondWithMediaType(`application/json`) {
            onComplete(mysqlService.search(search.text)) {
              case Success(value) => complete {
                write(value)
              }
              case Failure(e) => complete {
                "{}"
              }
            }
          }
        }
      }
    }
}
