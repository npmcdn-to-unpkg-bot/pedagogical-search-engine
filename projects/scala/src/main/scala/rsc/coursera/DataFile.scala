package rsc.coursera

import java.io.File

import rsc.coursera.Types.{Course, Courses, Domain, Domains}
import rsc.extraction.Data
import utils.Conversions.toBuffer

import scala.collection.mutable

class DataFile(alreadyRan: Boolean) extends Data[Course] {

  val in = new File(settings.Resources.Coursera.data)

  // Extract each article
  val courses = alreadyRan match {
    case false => {
      // The initial format is a bit weird
      val domains = parsed.extract[Domains]

      // Convert it
      toBuffer(domains.flatMap(flatten(_, None, None)))
    }
    case true => toBuffer(parsed.extract[Courses])
  }

  def flatten(domain: Domain, domStr: Option[String], subDomStr: Option[String]): Courses =
    domain.subdomain match {
      case Some(subdomains) => subdomains.flatMap(subdomain => domStr match {
        case Some(_) => flatten(subdomain, domStr, Some(subdomain.label))
        case None => flatten(subdomain, Some(subdomain.label), None)
      })
      case None => domain.courses match {
        case Some(courses) => courses.map(_.copy(domain = domStr, subdomain = subDomStr))
      }
    }

  // Methods to implement
  override def data: mutable.Buffer[Course] = courses

  override def copy(o: Course, newStatus: String): Course = o.copy(status = Some(newStatus))

  override def flushAfter: Int = 10
}
