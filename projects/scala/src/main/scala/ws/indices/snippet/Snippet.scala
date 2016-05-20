package ws.indices.snippet

import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import rsc.snippets.Source
import utils.StringUtils
import ws.indices.spraythings.SearchTerm

case class Snippet(lines: List[Line]) {

  implicit val format = DefaultFormats

  def toJSONString: String =
    org.json4s.native.Serialization.write(lines)

}

object Snippet  {
  implicit val format = DefaultFormats

  def fromText(text: String): Snippet =
    Snippet(List(Line(text, Nil)))

  def spotsFrom(text: String, searchTerms: TraversableOnce[SearchTerm]): TraversableOnce[Spot] =
    searchTerms.flatMap {
      case SearchTerm(label, oUri) =>
        StringUtils.indicesOf(text.toLowerCase(), label.toLowerCase()).map {
          case i => Spot(i, i + label.length, oUri.getOrElse(label.toLowerCase()))
        }
    }

  def fromSnippetJSON(jsonStr: String): Snippet = {
    val json = parse(jsonStr)
    json.extract[Snippet]
  }

  def fromRscSnippet(rscSnippet: rsc.snippets.Snippet, _uris: Set[String]): Snippet = {
    // normalize
    val uris: Set[String] = _uris.map(_.toLowerCase)

    // Get the top line if it is not the title
    val topLine = rscSnippet.topLine
    val topSnippet = topLine.source == Source.title match {
      case true => Nil
      case false => List(lineFromRscLine(topLine, uris))
    }

    // Produce other lines
    val remaining = 3 - topSnippet.size
    val pumped = pumpNTocLines(remaining, rscSnippet.otherLines, uris)

    // Produce the snippet
    Snippet(topSnippet:::pumped)
  }


  private def pumpNTocLines(n: Int, lines: List[rsc.snippets.Line], uris: Set[String])
  : List[Line] = {
    // Extract the indices that match
    val withMatches = lines.map(line => {
      val matches = line.indices.filter(index => uris.contains(index.uri))
      (line, matches)
    })

    // Take the best lines
    val tocLines = withMatches.filter(_._1.source == Source.toc)
    val ordered = tocLines.sortBy {
      case (line, matches) => (-matches.size, line.priority)
    }
    val topN = ordered.take(n).map(_._1)

    // Produce the lines
    topN.map(lineFromRscLine(_, uris))
  }

  private def lineFromRscLine(line: rsc.snippets.Line, uris: Set[String])
  : Line = {
    val matched = line.indices.filter(index => uris.contains(index.uri))
    val spots = matched.map(index => Spot(index.start, index.stop, index.uri))
    Line(line.text, spots)
  }
}
