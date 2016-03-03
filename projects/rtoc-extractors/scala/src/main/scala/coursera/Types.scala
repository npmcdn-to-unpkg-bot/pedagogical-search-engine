package coursera

object Types {
  case class Domain(href: String, label: String, subdomain: Option[List[Domain]],
                    courses: Option[List[Course]])
  case class Course(href: String, label: String, partner: Option[String],
                    remoteImg: String, localImg: String, localPath: String,
                    screenshot: String, domain: Option[String], subdomain: Option[String],
                    status: Option[String])
}
