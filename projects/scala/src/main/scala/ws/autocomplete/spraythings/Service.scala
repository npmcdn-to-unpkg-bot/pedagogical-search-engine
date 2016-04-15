package ws.autocomplete.spraythings

import org.json4s._
import org.json4s.native.Serialization.write
import spray.http.MediaTypes.`application/json`
import spray.routing.HttpService
import ws.autocomplete.MysqlService
import ws.autocomplete.spraythings.JsonSupport._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait Service extends HttpService with CORSSupport {

  val mysqlService = new MysqlService()
  implicit val formats = DefaultFormats

  val myRoute =
    path("autocomplete") {
      respondWithCORS() {
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
}
