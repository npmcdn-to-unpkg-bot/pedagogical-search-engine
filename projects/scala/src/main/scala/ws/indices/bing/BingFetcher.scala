package ws.indices.bing

import java.nio.charset.StandardCharsets

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import org.json4s.JsonAST.JObject
import org.parboiled.common.Base64
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.util._
import utils.Settings
import ws.indices.bing.BingJsonProtocol._

import scala.concurrent.Future
import scala.concurrent.duration._

class BingFetcher(timeoutMs: Int) {

  // Search for a text
  def search(text: String, from: Int, to: Int)
  : Future[BingApiResult] = {
    val uri = generateUri(text, from, to)
    pipeline(Get(uri))
  }

  // Private things
  implicit private val system = ActorSystem("search-bing")
  import system.dispatcher

  private def getCredits(apiKey: String): String = {
    val user = apiKey
    val password = apiKey
    val bytes = s"$user:$password".getBytes(StandardCharsets.UTF_8)
    Base64.rfc2045().encodeToString(bytes, false)
  }

  private def extractBingApiResult(json: JObject): BingApiResult = {
    val transformed = json.transformField {
      case ("Title", x) => ("title", x)
      case ("Description", x) => ("description", x)
      case ("Url", x) => ("url", x)
      case ("ID", x) => ("id", x)
    }
    transformed.extract[BingApiResult]
  }

  private val settings = new Settings()
  private val credits = getCredits(settings.Indices.Bing.apiKey)
  private val timeout = timeoutMs.milliseconds
  private val pipeline: HttpRequest => Future[BingApiResult] = (
      addHeader("Accept", "application/json")
      ~> addHeader("Authorization", s"Basic $credits")
      ~> sendReceive(implicitly, implicitly, futureTimeout = timeout)
      ~> unmarshal[JObject]
      ~> extractBingApiResult
    )

  private def generateUri(search: String, from: Int, to: Int)
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

    val size = to - from
    Uri(apiUrl).withQuery(
      ("$format", "json"),
      ("$skip", from.toString),
      ("$top", size.toString),
      ("Options", stringify("DisableLocationDetection")),
      ("Market", stringify("en-us")),
      ("Query", stringify(s"$sitesStr $languageStr $search"))
    )
  }

  private def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(3.second).await
    system.shutdown()
  }
}
