package rsc.safaribooks

import rsc.extraction.HasStatus

object Types {
  case class Book(path: String, status: Option[String]) extends HasStatus

  case class Metadata(title: String, publisher: String)
}
