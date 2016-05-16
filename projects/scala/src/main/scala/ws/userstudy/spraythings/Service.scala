package ws.userstudy.spraythings

import org.json4s.Formats
import rsc.Formatters
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import ws.spraythings.CORSSupport
import ws.userstudy.Executor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait Service extends HttpService with CORSSupport with Json4sSupport with Formatters {

  override implicit def json4sFormats: Formats = formats

  val executor = new Executor()

  val OkMsg = "OK"
  val FailMsg = "FAILED"

  val myRoute =
    path("clicks") {
      respondWithCORS() {
        post {
          entity(as[ClickInput]) { ci =>
            onComplete(executor.saveClick(ci)) {
              case Success(value) => complete { OkMsg }
              case Failure(e) => complete { FailMsg }
            }
          }
        }
      }
    } ~
      path("classifications") {
        respondWithCORS() {
          post {
            entity(as[ClassificationInput]) { ci =>
              onComplete(executor.saveCl(ci)) {
                case Success(value) => complete { OkMsg }
                case Failure(e) => complete { FailMsg }
              }
            }
          }
        }
      }
}
