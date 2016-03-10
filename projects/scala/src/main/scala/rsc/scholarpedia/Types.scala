package rsc.scholarpedia

import rsc.HasStatus

object Types {
  case class Article(label: String,
                     href: String,
                     page: String,
                     status: Option[String]) extends HasStatus
  case class ArticleEntry(label: String,
                          href: String,
                          page: Option[String],
                          status: Option[String])
  type ArticlesEntries = List[ArticleEntry]
}
