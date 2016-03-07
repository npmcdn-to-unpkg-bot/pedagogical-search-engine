package rsc

import org.jsoup.nodes.Document


trait LayoutExtractor {
  // To implement
  def unapply(doc: Document): Option[ResourceElement]
}
