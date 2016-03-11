package spotlight

import java.util.concurrent.TimeoutException

import dispatch.Defaults._
import dispatch._

import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import org.json4s.native.JsonMethods._

import spotlight.Types.Spots
import spotlight.Types.WebService.Annotation

import scala.concurrent.Await
import scala.concurrent.duration._


class WebService(wsHost: String, wsPort: Int) {
  implicit val formats = DefaultFormats

  def launchAnnotation(text: String): Future[Spots] = {
    // Create the request
    val myHost = host(wsHost, wsPort)
    val myRequest = (myHost / "rest" / "candidates")
      .addHeader("Accept", "application/json") << Map(
      "text" -> text,
      "confidence" -> "0",
      "support" -> "0",
      "coreferenceResolution" -> "false"
    )

    val response = Http(myRequest.POST OK dispatch.as.json4s.Json)

    // Produce the spots
    response.map(json => {
      // Rename fields with problematic identifiers
      val renamed = Types.WebService.transformFields(json \ "annotation")

      // Normalize the json
      val normalized = renamed transformField {
        case ("surfaceForms", x: JObject) => ("surfaceForms", JArray(x :: Nil))
        case ("resources", x: JObject) => ("resources", JArray(x :: Nil))

        case ("offset", JString(s)) => ("offset", JInt(s.toInt))
        case ("support", JString(s)) => ("support", JInt(s.toInt))

        case ("contextualScore", JString(s)) => ("contextualScore", JDouble(s.toDouble))
        case ("percentageOfSecondRank", JString(s)) => ("percentageOfSecondRank", JDouble(s.toDouble))
        case ("priorScore", JString(s)) => ("priorScore", JDouble(s.toDouble))
        case ("finalScore", JString(s)) => ("finalScore", JDouble(s.toDouble))
      }

      // Extract the annotations
      val annotation = normalized.extract[Annotation]

      // Produce the spots
      annotation.spots()
    })
  }

  def launchAnnotations(texts: List[String]): Future[List[Spots]] = {
    val separated: List[Future[Spots]] = texts.map(launchAnnotation(_))
    val merged: Future[List[Spots]] = Future.sequence(separated)
    merged
  }

  def textsToSpots(texts: List[String]): Option[List[Spots]] = {
    val future = launchAnnotations(texts)
    // Estimate waiting time
    val time = (30 * texts.size) seconds

    try {
      val spots: List[Spots] = Await.result(future, time)
      Some(spots)
    } catch {
      case e: TimeoutException => None
    }
  }

  def textToSpots(text: String): Option[Spots] = textsToSpots(text::Nil) match {
    case None => None
    case Some(l) => Some(l.head)
  }

  // todo: Keep an eye on https://github.com/dispatch/reboot/issues/59
  def shutdown = Http.shutdown()
}
