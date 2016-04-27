package ws.indices.bing

import akka.actor.ActorSystem
import akka.event.Logging
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.util._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import BingJsonProtocol._
import org.json4s.JsonAST.JObject
import java.nio.charset.StandardCharsets

import org.parboiled.common.Base64
import utils.Settings

object Query extends App {
  implicit val system = ActorSystem("search-bing")
  import system.dispatcher
  val log = Logging(system, getClass)

  def getCredits(apiKey: String): String = {
    val user = apiKey
    val password = apiKey
    val bytes = s"$user:$password".getBytes(StandardCharsets.UTF_8)
    Base64.rfc2045().encodeToString(bytes, false)
  }

  def extractBingApiResult(json: JObject): BingApiResult = {
    val transformed = json.transformField {
      case ("Title", x) => ("title", x)
      case ("Description", x) => ("description", x)
      case ("Url", x) => ("url", x)
    }
    transformed.extract[BingApiResult]
  }

  val settings = new Settings()
  val credits = getCredits(settings.Indices.Bing.apiKey)
  val pipeline: HttpRequest => Future[BingApiResult] = (
      addHeader("Accept", "application/json")
      ~> addHeader("Authorization", s"Basic $credits")
      ~> sendReceive
      ~> unmarshal[JObject]
      ~> extractBingApiResult
    )

  def generateUri(search: String)
  : Uri = {
    val searchSpace = List(
      "coursera.org",
      "ocw.mit.edu/courses",
      "safaribooksonline.com",
      "scholarpedia.org/article",
      "www.khanacademy.org"
    )
    val sitesStr = "(" + searchSpace.map(site => s"site:$site").mkString(" OR ") + ")"

    val languageStr = "language:en"

    val apiUrl = "https://api.datamarket.azure.com/Bing/SearchWeb/v1/Web"

    def stringify(s: String): String = s"'$s'"
    Uri(apiUrl).withQuery(
      ("$format", "json"),
      ("$skip", "0"),
      ("$top", "50"),
      ("Options", stringify("DisableLocationDetection")),
      ("Market", stringify("en-us")),
      ("Query", stringify(s"$sitesStr $languageStr $search"))
    )
  }

  val someSearches = List(
    "Coherent activity in excitatory pulse-coupled networks",
    "Astrometry",
    "Minimal dynamical systems",
    "Machine learning algorithms svm",
    "Artificial intelligence finite state transducers",
    "Convex optimization simplex algorithm",
    "Compiler construction",
    "physic velocity",
    "trigonometry introduction",
    "svm expectation maximization"
  )

  def search(n: Int, sum: Int): Unit = {
    val start = System.nanoTime()
    val searchText = someSearches(math.floor(math.random * someSearches.size).toInt)
    val uri = generateUri(searchText)
    val response: Future[BingApiResult] = pipeline { Get(uri) }

    response.onComplete {
      case Success(bingApiResult) => {
        val stop = System.nanoTime()
        val elapsed = utils.Utils.elapsedMs(start)
        println(s"time: $elapsed, count: ${bingApiResult.d.results.size}, text: $searchText")
        //bingApiResult.d.results.map(r => println(r.url + ": " + r.title))
        if(n < 20) {
          search(n + 1, sum + elapsed)
        } else {
          val avg = sum.toDouble / n.toDouble
          println(s"avg: $avg")
          shutdown()
        }
      }
      case Failure(error) => {
        log.error(error, "Couldn't search with bing")
        shutdown()
      }
    }
  }
  search(1, 0)

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }
}
