package rsc.mit

import rsc.HasStatus

object Types {
  case class Course(courseNumber: String,
                    href: String,
                    level: String,
                    uniqueName: String,
                    pages: List[Page],
                   // New fields
                    status: Option[String]
                   ) extends HasStatus
  case class Page(href: String,
                  normalizedLabel: String,
                  localPath: String,
                  status: String
                 // New fields
                 ) {
    def isHome = isIn(Labels.home)
    def isSyllabus = isIn(Labels.syllabus)
    def isCalendar = isIn(Labels.calendar)
    def isReadings = isIn(Labels.readings)
    def isAssignments = isIn(Labels.assignments)
    def isProjects = isIn(Labels.projects)
    def isRelatedResource = isIn(Labels.relatedResources)
    def isDownload = isIn(Labels.downloads)

    def isIn(l: List[String]): Boolean = l.contains(normalizedLabel)
  }
  type Courses = List[Course]
  type Pages = List[Page]
}

object Labels {
  val home = List("course home")
  val syllabus = List("syllabus")
  val calendar = List("calendar")
  val readings = List("readings")
  val assignments = List("assignments")
  val projects = List("projects")
  val relatedResources = List("related resources")
  val downloads = List("download course materials")
}
