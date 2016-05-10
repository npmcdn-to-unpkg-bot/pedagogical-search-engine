package ws.indices.spraythings

import org.json4s.Formats
import org.json4s.native.Serialization.write
import rsc.Formatters
import spray.http.MediaTypes.`application/json`
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import utils.Settings
import ws.indices.SearchExecutor
import ws.spraythings.CORSSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait Service extends HttpService with CORSSupport with Json4sSupport with Formatters {

  override implicit def json4sFormats: Formats = formats

  val executor = new SearchExecutor(new Settings())

  val myRoute =
    path("indices") {
      respondWithCORS() {
        post {
          entity(as[Search]) { search =>
            respondWithMediaType(`application/json`) {
              onComplete(executor.search(search)) {
                case Success(value) => complete {
                  value
                }
                case Failure(e) => complete {
                  e.printStackTrace()
                  List()
                }
              }
            }
          }
        }
      }
    }
}
