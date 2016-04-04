package spotlight

import dispatch.Defaults._
import dispatch._
import org.json4s.DefaultFormats
import org.json4s.JsonAST._
import rsc.Types.Spots
import rsc.attributes.Spot
import spotlight.Types.Annotation

import scala.concurrent.ExecutionContext


class LazyWebService(wsHost: String, wsPort: Int, ec: ExecutionContext) {
  implicit val formats = DefaultFormats

  val http = Http.configure(_.
    setRequestTimeout(5 * 1000).
    setMaxRequestRetry(5).
    setAllowPoolingConnections(true))

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

    val response = http(myRequest.POST OK dispatch.as.json4s.Json)(ec)

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
    val separator = "\n"
    val merged = texts.mkString(separator)
    val annotation = annotate(merged)

    // But keep the starting offsets
    val sepSize = separator.size
    type OffsetWithSpots = (Int, List[Spot])
    val offsets = texts.map(_.size).foldLeft((0, List[OffsetWithSpots]())) {
      case ((offset, acc), size) => (offset + size + sepSize, (offset, Nil)::acc)
    } match {
      case (_, acc) => acc
    }

    // Convert a position into the starting position of its corresponding text
    // e.g. texts = List(hello, john)
    // e.g. realOffset(3) = 0, ro(8) = 5, ro(5) = 5, ro(4) = 0
    def realOffset(pos: Int): Int = {
      def rec(pos: Int, last: OffsetWithSpots, next: List[OffsetWithSpots])
      : OffsetWithSpots = next match {
        case Nil => last
        case head::tail => head match {
          case (offset, spots) => (pos < offset) match {
            case true => last
            case false => rec(pos, head, tail)
          }
        }
      }
      rec(pos, (0, Nil), offsets.sortBy { case (offset, _) => offset }) match {
        case (offset, _) => offset
      }
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
        }.toList.map {
          // drop the offset duplicate
          case (offset, l) => {
            val spots = l.map(_._2)
            (offset, spots)
          }
        } toMap

        // Put them in the same order as the input texts
        val completed = offsets.map {
          case (offset, spots) => grouped.contains(offset) match {
            case true => (offset, grouped(offset))
            case false => (offset, Nil)
          }
        }
        val ordered = completed.sortBy {
          case (offset, _) => offset
        }

        // Extract each chunk of spots
        val newSpots: List[Spots] = ordered.map {
          case (offset, spots) => spots
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
