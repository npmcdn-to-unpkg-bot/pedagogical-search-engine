package rtoc

import java.io.File

abstract class Data[U](in: File) {
  var pos = 0
  var flushCount = 0

  // Iterator aspect
  def init() = { pos = 0 }
  def next(): U = {
    val value = get(pos) match {
      case Some(x) => x
    }
    pos = pos + 1
    value
  }
  def hasNext(): Boolean = get(pos).isDefined

  def get(i: Int): Option[U]

  // Edit aspect
  def markNotOk(entry: U) = lazyMark(entry, "not-ok")
  def markOk(entry: U) = lazyMark(entry, "ok")

  def lazyMark(entry: U, s: String): Unit = {
    // Mark the entry
    mark(entry, s)

    // Flush lazily
    flushCount = flushCount + 1
    if(flushCount > 10) {
      flush()
      flushCount = 0
    }
  }
  def flush(): Unit = flushCount match {
    case 0 => {}
    case _ => executeFlush()
  }

  def mark(entry: U, s: String): Unit
  def executeFlush(): Unit
}
