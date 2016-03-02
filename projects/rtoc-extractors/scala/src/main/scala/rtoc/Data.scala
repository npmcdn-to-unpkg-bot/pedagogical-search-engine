package rtoc

import java.io.File

abstract class Data[U](in: File) {
  val status = "rtoc-status"
  var pos = 0

  // Iterator aspect
  def init() = { pos = 0 }
  def next(): U = {
    pos = pos + 1
    get(pos) match {
      case Some(x) => x
      case None => ???
    }
  }
  def hasNext(): Boolean = get(pos + 1).isDefined

  def get(i: Int): Option[U]

  // Edit aspect
  def markDone(entry: U) = mark(entry, status, "done")
  def markNotOk(entry: U) = mark(entry, status, "not-ok")
  def markOk(entry: U) = mark(entry, status, "ok")
  def bindTo(entry: U, resources: List[Resource]) = mark(entry, "resources", resources.map(_.path()))

  def mark(entry: U, name: String, s: String): Unit = mark(entry, name, s::Nil)
  def mark(entry: U, name: String, xs: List[String]): Unit
  def flush(): Unit
}
