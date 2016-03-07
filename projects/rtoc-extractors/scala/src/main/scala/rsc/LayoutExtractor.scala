package rsc

import org.jsoup.nodes.Document


abstract class LayoutExtractor {

  // Either override this method, or use the
  // default implementation
  def unapply(doc: Document): Option[ResourceElement] = javaStyle(doc, getOrFail)

  // .. but then you should implement this
  @throws(classOf[Exception])
  protected def getOrFail(doc: Document): ResourceElement = throw new Exception("You should implement this method.")

  // Internal things
  private def javaStyle(doc: Document, fn: Document => ResourceElement): Option[ResourceElement] = {
    try {
      Some(fn(doc))
    } catch {
      case e => None
    }
  }
}
