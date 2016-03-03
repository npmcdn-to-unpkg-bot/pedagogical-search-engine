package rtoc

import java.io.File

import Utils.Logger

abstract class Data[U](in: File) {
  var pos = 0
  var flushCount = 0

  val notOk = "not-ok"
  val ok = "ok"

  object Marked {
    def unapply(s: String): Boolean = (s.equals(notOk) || s.equals(ok))
  }

  // Iterator aspect
  def init() = { pos = 0 }
  def nextUndone(): Int = {
    def getNext(i: Int): Int = get(i) match {
      case None => -1
      case Some(o) => getMark(o) match {
        case Some(mark) => mark match {
          case Marked() => getNext(i + 1)
          case _ => i
        }
        case None => i
      }
    }
    getNext(pos)
  }
  def next(): U = {
    pos = nextUndone()
    val value = get(pos) match {
      case Some(x) => x
    }
    pos = pos + 1
    value
  }
  def hasNext(): Boolean = nextUndone() match {
    case -1 => false
    case _ => true
  }

  def get(i: Int): Option[U]

  // Edit aspect
  def markNotOk(entry: U) = lazyMark(entry, notOk)
  def markOk(entry: U) = lazyMark(entry, ok)

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

  // Abstract methods
  def apply[V](entry: U, f: Int => V): V
  def mark(entry: U, s: String): Unit
  def getMark(entry: U): Option[String]
  def executeFlush(): Unit
}
