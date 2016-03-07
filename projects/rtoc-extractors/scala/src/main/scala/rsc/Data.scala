package rsc

import java.io.{PrintWriter, File}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.writePretty

abstract class Data[U <: HasStatus](in: File) {
  var pos = 0
  var flushCount = 0

  val notOk = "not-ok"
  val ok = "ok"

  object Marked {
    def unapply(s: String): Boolean = passEntry(s)
  }

  // Parse aspect
  implicit val formats = DefaultFormats
  val parsed = parse(in)

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

  // Methods that may be overriden
  def get(i: Int): Option[U] = (i < data.size && i > -1) match {
    case false => None
    case true => Some(data(i))
  }

  def executeFlush(): Unit = {
    // Serialize data
    val c = writePretty(data)
    val pw = new PrintWriter(in)

    // Write
    pw.write(c)
    pw.close()
  }

  def mark(entry: U, s: String): Unit = apply(entry, i => {
    data(i) = copy(data(i), s)
  })

  def getMark(entry: U): Option[String] = apply(entry, i => data(i).status)

  def passEntry(label: String): Boolean = label.equals(ok)

  // other methods
  def apply[V](entry: U, f: Int => V): V = data.indexOf(entry) match {
    case index if index > -1 => f(index)
  }

  // Methods to implement
  def data: scala.collection.mutable.Buffer[U]

  def copy(o: U, newStatus: String): U
}
