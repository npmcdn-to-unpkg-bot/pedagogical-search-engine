package rsc.extraction

import java.io.File

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import rsc.Resource
import utils.Settings

abstract class Factory[U <: HasStatus](_settings: Settings) {

  def this() = this(new Settings())
  val settings = _settings

  // Either override this method, or use the
  // default implementation
  def getResource(data: U): Option[Resource] = javaStyle(data, getOrFail)

  // .. but then you should implement this
  @throws(classOf[Exception])
  protected def getOrFail(data: U): Resource = throw new Exception("You should implement this method.")

  // Internal things
  private def javaStyle(data: U, fn: U => Resource): Option[Resource] = {
    try {
      Some(fn(data))
    } catch {
      case e => None
    }
  }

  // other methods
  def open(path: String): Document = Jsoup.parse(new File(path), "UTF-8")

  def openWeird(folder: String, filePath: String): Document = {
    val fileName = new File(filePath).getName
    open(s"$folder/$fileName")
  }
}
