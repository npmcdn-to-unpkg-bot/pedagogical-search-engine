package mit

import java.io.{File, PrintWriter}

import mit.Types.{Courses, Course}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.writePretty
import rtoc.Data

import utils.Conversions.toBuffer

class DataFile(in: File) extends Data[Course](in) {

  // Parse data
  implicit val formats = DefaultFormats
  val parsed = parse(in)

  // Extract each article
  val courses = toBuffer(parsed.extract[Courses])

  def flatten(domain: , domStr: Option[String], subDomStr: Option[String]): Courses =
    domain.subdomain match {
      case Some(subdomains) => subdomains.flatMap(subdomain => domStr match {
        case Some(_) => flatten(subdomain, domStr, Some(subdomain.label))
        case None => flatten(subdomain, Some(subdomain.label), None)
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
  }

  override def getMark(entry: Course): Option[String] = apply(entry, i => courses(i).status)

  override def passEntry(label: String): Boolean = label.equals(ok)
}
