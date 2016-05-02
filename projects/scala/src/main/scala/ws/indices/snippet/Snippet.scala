package ws.indices.snippet

import org.json4s.DefaultFormats
import rsc.snippets.Source

case class Snippet(lines: List[Line]) {

  implicit val format = DefaultFormats

  def toJSONString: String =
    org.json4s.native.Serialization.write(lines)

}

object Snippet {
  def fromText(text: String): Snippet =
    Snippet(List(Line(text, Nil)))

  def fromRscSnippet(rscSnippet: rsc.snippets.Snippet, uris: Set[String]): Snippet = {
    // Get the top line if it is not the title
    val topLine = rscSnippet.topLine
    val topSnippet = topLine.source == Source.title match {
      case true => Nil
      case false => List(lineFromRscLine(topLine, uris))
    }

    // Produce other lines
    val remaining = 3 - topSnippet.size
    val pumped = pumpNLines(remaining, rscSnippet.otherLines, uris)

    // Produce the snippet
    Snippet(topSnippet:::pumped)
  }


  private def pumpNLines(n: Int, lines: List[rsc.snippets.Line], uris: Set[String])
  : List[Line] = {
    // Extract the indices that match
    val withMatches = lines.map(line => {
      val matches = line.indices.filter(index => uris.contains(index.uri))
      (line, matches)
    })

    // Take the best lines
    val filtered = withMatches.filter(_._2.nonEmpty)
    val tocLines = filtered.filter(_._1.priority != 1)
    val ordered = tocLines.sortBy {
      case (line, matches) => (line.priority, -matches.size)
    }
    val topN = ordered.take(n)

    // Produce the lines
    topN.map(_._1).map(lineFromRscLine(_, uris))
  }

  private def lineFromRscLine(line: rsc.snippets.Line, uris: Set[String])
  : Line = {
    val matched = line.indices.filter(index => uris.contains(index.uri))
    val spots = matched.map(index => Spot(index.start, index.stop, index.uri))
    Line(line.text, spots)
  }
}
