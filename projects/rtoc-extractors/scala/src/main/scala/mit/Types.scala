package mit

import rtoc.HasStatus

object Types {
  case class Course(number: String,
                    href: String,
                    level: String,
                    uniqueName: String,
                    pages: String,
                   // New fields
                    status: Option[String]
                   ) extends HasStatus
  case class Page(href: String,
                  normalizedLabel: String,
                  localPath: String,
                  status: String
                 // New fields
                 )
  type Courses = List[Course]
  type Pages = List[Page]
}
