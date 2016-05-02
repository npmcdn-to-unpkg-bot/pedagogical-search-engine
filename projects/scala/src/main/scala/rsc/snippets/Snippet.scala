package rsc.snippets

import org.json4s.native.JsonMethods.parse
import ws.indices.Formatters

case class Snippet(topLine: Line,
                   otherLines: List[Line]) {
  def size(): Int = 1 + otherLines.size
}

object Snippet extends Formatters {
  implicit private val formatter = formats

  def fromJSONString(jsonStr: String): Snippet = {
    val json = parse(jsonStr)
    json.extract[rsc.snippets.Snippet]
  }
}
