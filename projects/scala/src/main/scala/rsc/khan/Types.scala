package rsc.khan

import rsc.HasStatus

object Types {
  case class Course(parents: List[String], path: String, status: Option[String]) extends HasStatus
}
