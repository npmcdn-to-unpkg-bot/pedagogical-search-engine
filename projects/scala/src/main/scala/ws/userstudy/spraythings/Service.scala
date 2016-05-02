package ws.userstudy.spraythings

import spray.routing.HttpService
import ws.userstudy.Executor
import ws.userstudy.spraythings.JsonSupport._
import ws.spraythings.CORSSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait Service extends HttpService with CORSSupport {

  val executor = new Executor()

  val OkMsg = "OK"
  val FailMsg = "FAILED"

  val myRoute =
    path("searches") {
      respondWithCORS() {
        post {
          entity(as[SearchInput]) { si =>
            onComplete(executor.saveSearch(si.uris)) {
              case Success(value) => complete { OkMsg }
              case Failure(e) => complete { FailMsg }
            }
          }
        }
      }
    } ~
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
