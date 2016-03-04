package rtoc

import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.collection.JavaConverters._

trait LayoutExtractor[U] {
  def l(es: Elements): List[Element] = es.iterator().asScala.toList

  // To implement
  def unapply(doc: Document): Option[U]
}
