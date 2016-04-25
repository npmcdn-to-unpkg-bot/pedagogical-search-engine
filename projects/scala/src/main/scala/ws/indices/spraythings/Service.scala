package ws.indices.spraythings

import org.json4s._
import org.json4s.native.Serialization.write
import rsc.Formatters
import spray.http.MediaTypes.`application/json`
import spray.routing.HttpService
import ws.indices.MysqlService
import ws.indices.spraythings.JsonSupport._
import ws.spraythings.CORSSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait Service extends HttpService with CORSSupport with Formatters {

  val mysqlService = new MysqlService()

  val myRoute =
    path("indices") {
      respondWithCORS() {
        post {
          entity(as[Search]) { search =>
            respondWithMediaType(`application/json`) {
              onComplete(mysqlService.search(search.uris, search.from, search.to)) {
                case Success(value) => complete {
                  write(value)
                }
                case Failure(e) => complete {
                  "[]"
                }
              }
            }
          }
        }
      }
    }
}
