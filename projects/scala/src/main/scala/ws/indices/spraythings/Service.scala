package ws.indices.spraythings

import org.json4s.native.Serialization.write
import rsc.Formatters
import spray.http.MediaTypes.`application/json`
import spray.routing.HttpService
import utils.Settings
import ws.indices.SearchExecutor
import ws.indices.spraythings.JsonSupport._
import ws.spraythings.CORSSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait Service extends HttpService with CORSSupport with Formatters {

  val executor = new SearchExecutor(new Settings())

  val myRoute =
    path("indices") {
      respondWithCORS() {
        post {
          entity(as[Search]) { search =>
            respondWithMediaType(`application/json`) {
              onComplete(executor.search(search.uris, search.from, search.to)) {
                case Success(value) => complete {
                  write(value)
                }
                case Failure(e) => complete {
                  e.printStackTrace()
                  "[]"
                }
              }
            }
          }
        }
      }
    }
}
