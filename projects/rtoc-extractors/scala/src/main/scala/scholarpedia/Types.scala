package scholarpedia

object Types {
  case class Downloaded(label: String, href: String, page: String)
  case class Article(label: String, href: String, status: Option[String], page: Option[String])
}
