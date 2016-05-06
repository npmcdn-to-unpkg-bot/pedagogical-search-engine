package rsc

import rsc.attributes.Source._

object Utils {
  def getUrl(r: Resource): Option[String] = r.oHref match {
    case None => None
    case Some(partialHref) => r.source match {
      case Coursera => Some(s"https://www.coursera.org$partialHref")
      case Khan => Some(s"https://www.khanacademy.org$partialHref")
      case MIT => Some(s"http://ocw.mit.edu$partialHref")
      case Safari => None
      case Scholarpedia => Some(s"http://www.scholarpedia.org$partialHref")
    }
  }
}
