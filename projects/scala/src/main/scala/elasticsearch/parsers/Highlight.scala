package elasticsearch.parsers

import ws.indices.snippet.{Line, Spot}

object Highlight {
  private val openTag = "<em>"
  private val closeTag = "</em>"

  def parse(text: String): Line = {
    def rec(t: String): List[Term] = {
      val index1 = t.indexOf(openTag)
      val index2 = t.indexOf(closeTag)

      if(index1 == -1 || index2 == -1) {
        List(ContextualText(t))
      } else {
        val a = t.substring(0, index1)
        val b = t.substring(index1 + openTag.length, index2)
        val c = t.substring(index2 + closeTag.length)
        ContextualText(a)::HighlightedText(b)::rec(c)
      }
    }

    val terms = rec(text)

    val cleanText = terms.map {
      case ContextualText(t) => t
      case HighlightedText(t) => t
    }.mkString("")

    val initAcc = List[Spot]()
    val spots = terms.foldLeft((0, initAcc)) {
      case ((pos, spots), term) => term match {
        case ContextualText(t) => (pos + t.length, spots)
        case HighlightedText(t) => {
          val nextPos = pos + t.length
          val newSpot = Spot(pos, nextPos, t)
          (nextPos, spots:::List(newSpot))
        }
      }
    }._2

    Line(cleanText, spots)
  }
}