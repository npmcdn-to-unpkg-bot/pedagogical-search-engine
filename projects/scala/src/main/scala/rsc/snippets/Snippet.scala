package rsc.snippets

case class Snippet(topLine: Line,
                   otherLines: List[Line]) {
  def size(): Int = 1 + otherLines.size
}
