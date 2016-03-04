package coursera

import java.io.{File, PrintWriter}

import Utils.Logger
import coursera.Types.{Course, Domain}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.writePretty
import rtoc.Data

import scala.collection.mutable

class DataFile(in: File, alreadyRunned: Boolean) extends Data[Course](in) {

  // Parse data
  implicit val formats = DefaultFormats
  val parsed = parse(in)

  // Extract each article
  val courses = alreadyRunned match {
    case false => {
      // The initial format is a bit weird
      val domains = parsed.extract[List[Domain]]

      // Convert it
      toBuffer(domains.flatMap(flatten(_, None, None)))
    }
    case true => toBuffer(parsed.extract[List[Course]])
  }
  def toBuffer(a: List[Course]): mutable.Buffer[Course] = mutable.Buffer().++(a)

  def flatten(domain: Domain, domStr: Option[String], subDomStr: Option[String]): List[Course] =
    domain.subdomain match {
      case Some(subdomains) => subdomains.flatMap(subdomain => domStr match {
        case Some(_) => flatten(subdomain, domStr, Some(domain.label))
        case None => flatten(subdomain, Some(domain.label), None)
      })
      case None => domain.courses match {
        case Some(courses) => courses.map(_.copy(domain = domStr, subdomain = subDomStr))
      }
    }

  // Implement abstract methods
  override def get(i: Int): Option[Course] = (i < courses.size && i > -1) match {
    case false => None
    case true => Some(courses(i))
  }

  override def mark(entry: Course, s: String): Unit = apply(entry, i => {
    courses(i) = courses(i).copy(status = Some(s))
  })

  override def apply[V](entry: Course, f: Int => V): V = courses.indexOf(entry) match {
    case -1 => ???
    case index => f(index)
  }

  override def executeFlush(): Unit = {
    // Serialize data
    val c = writePretty(courses)
    val pw = new PrintWriter(in)

    // Write
    pw.write(c)
    pw.close()
    Logger.info(s"Courses flushed")
  }

  override def getMark(entry: Course): Option[String] = apply(entry, i => courses(i).status)
}
