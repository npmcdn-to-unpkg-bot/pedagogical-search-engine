package scholarpedia

object Types {
  case class Downloaded(label: String,
                        href: String,
                        page: String,
                        status: Option[String])
  case class Article(label: String,
                     href: String,
                     page: Option[String])
}
