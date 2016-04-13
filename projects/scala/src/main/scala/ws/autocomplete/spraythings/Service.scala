package ws.autocomplete.spraythings

import scala.util.{Failure, Success}
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.HttpService
import spray.http.StatusCodes.InternalServerError
import ws.autocomplete.MysqlService
import scala.concurrent.ExecutionContext.Implicits.global

case class Search(text: String)

object SearchJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PersonFormat = jsonFormat1(Search)
}

trait Service extends HttpService {

  val mysqlService = new MysqlService()
  import SearchJsonSupport._

  val myRoute =
    path("autocomplete") {
      post {
        entity(as[Search]) { search =>
          // todo: respondWithMediaType(`application/json`)
          onComplete(mysqlService.search(search.text)) {
            case Success(value) => complete(s"$value")
            case Failure(e) => complete(InternalServerError, e.getMessage)
          }
        }
      }
    }
}
