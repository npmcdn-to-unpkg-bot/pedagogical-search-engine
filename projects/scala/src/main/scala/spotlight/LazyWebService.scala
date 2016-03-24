package spotlight

import dispatch.Defaults._
import dispatch._
import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import rsc.Types.Spots
import rsc.attributes.Spot
import spotlight.Types.Annotation


class LazyWebService(wsHost: String, wsPort: Int) {
  implicit val formats = DefaultFormats

  def annotate(text: String)
  : Future[Spots] = {
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
      val renamed = Types.transformFields(json \ "annotation")

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

  def annotateTogether(texts: List[String])
  : Future[List[Spots]] = {
    // Annotated all the texts together
    val merged = texts.mkString("")
    val annotation = annotate(merged)

    // Convert a position into the starting position of its corresponding text
    // e.g. texts = List(hello, john)
    // e.g. realOffset(3) = 0, ro(8) = 5, ro(5) = 5, ro(4) = 0
    def realOffset(pos: Int): Int = {
      def rec(pos: Int, acc: List[String], start: Int): Int = acc match {
        case head::tail => {
          val end = start + head.size
          (pos < end) match {
            case true => start
            case false => rec(pos, tail, end)
          }
        }
      }
      rec(pos, texts, 0)
    }

    // Dispatch back the spots
    annotation.map {
      case spots => {
        // Get the real offset
        val withRealOffset = spots.map(spot => (realOffset(spot.start), spot))

        // Shift the spots
        val shifted = withRealOffset.map {
          case (offset, spot) => {
            // Shift the spots
            val shiftedStart = spot.start - offset
            val shifterEnd = spot.end - offset
            (offset, Spot(shiftedStart, shifterEnd, spot.candidates))
          }
        }

        // Group them by real offset
        val grouped = shifted.groupBy {
          case (offset, spot) => offset
        } toList

        // Put them in the same order as the input texts
        val ordered = grouped.sortBy {
          case (offset, _) => offset
        }

        // Extract each chunk of spots
        val newSpots: List[Spots] = ordered.map {
          case (offset, spotsPairs) => spotsPairs.map { case (o, spot) => spot }
        }

        newSpots
      }
    }
  }

  def annotateSeparately(texts: List[String]):
  Future[List[Spots]] = Future.sequence(texts.map(annotate(_)))

  def annotateSingle(text: String):
  Future[Spots] = annotate(text)

  // todo: Keep an eye on https://github.com/dispatch/reboot/issues/59
  def shutdown = Http.shutdown()
}
