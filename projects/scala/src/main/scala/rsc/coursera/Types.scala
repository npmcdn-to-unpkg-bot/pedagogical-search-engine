package rsc.coursera

import rsc.extraction.HasStatus

object Types {
  case class Domain(href: String, label: String, subdomain: Option[Domains],
                    courses: Option[Courses])
  case class Course(href: String, label: String, partner: Option[String],
                    remoteImg: String, localImg: String, localPath: String,
                    screenshot: String, domain: Option[String], subdomain: Option[String],
                    status: Option[String]) extends HasStatus {
    override def toString(): String = s"$href"
  }
  type Domains = List[Domain]
  type Courses = List[Course]
}
