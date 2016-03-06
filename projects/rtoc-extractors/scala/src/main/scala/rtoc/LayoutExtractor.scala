package rtoc

import org.jsoup.nodes.Document


trait LayoutExtractor[U] {
  // To implement
  def unapply(doc: Document): Option[U]
}
