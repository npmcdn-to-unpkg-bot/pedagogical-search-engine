package elasticsearch.parsers

abstract class Term {}

case class ContextualText(body: String) extends Term {}

case class HighlightedText(body: String) extends Term {}
