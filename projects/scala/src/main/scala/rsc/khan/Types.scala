package rsc.khan

import rsc.extraction.HasStatus

object Types {
  case class Course(parents: List[String], path: String, status: Option[String]) extends HasStatus

  case class Metadata(title: String, href: String)
}
