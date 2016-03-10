package rsc.safaribooks

import rsc.HasStatus

object Types {
  case class Book(path: String, status: Option[String]) extends HasStatus
}
