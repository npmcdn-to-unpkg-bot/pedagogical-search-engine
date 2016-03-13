package rsc.extraction

import org.jsoup.nodes.Document


abstract class LayoutExtractor[U] {

  // Either override this method, or use the
  // default implementation
  def unapply(doc: Document): Option[U] = javaStyle(doc, getOrFail)

  // .. but then you should implement this
  @throws(classOf[Exception])
  protected def getOrFail(doc: Document): U = throw new Exception("You should implement this method.")

  // Internal things
  private def javaStyle(doc: Document, fn: Document => U): Option[U] = {
    try {
      Some(fn(doc))
    } catch {
      case e => None
    }
  }
}
