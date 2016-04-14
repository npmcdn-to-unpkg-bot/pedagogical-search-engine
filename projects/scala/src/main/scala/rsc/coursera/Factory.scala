package rsc.coursera

import rsc.Resource
import rsc.attributes._
import rsc.coursera.Types.Course
import rsc.coursera.layouts._
import utils.StringUtils.textOf

class Factory extends rsc.extraction.Factory[Course]() {

  override def getOrFail(course: Course): Resource = {
    course.href.startsWith("/specializations") match {
      case false => {
        // Parse
        val doc = openWeird(settings.Resources.Coursera.pages, course.localPath)

        // Extract toc
        val href = course.href
        val oTocs = doc match {
          case toc.Simple(x) => Some(List(x))
          case toc.Inline(x) => Some(List(x))
          case _ => None
        }

        // Extract description
        val oDescriptions = doc match {
          case description.Simple(d) => Some(List(d))
          case description.Inline(d) => Some(List(d))
          case _ => None
        }

        // Test that the course has at least one of them
        if (oTocs.isDefined || oDescriptions.isDefined) {
          // Create the metadata
          val partnersV = course.partner match {
            case Some(partner) => Some(partner :: Nil)
            case None => None
          }
          val source = Source.Coursera
          val title = textOf(course.label) match {
            case t if t.length > 0 => t
          }

          val level = Level.University
          val href = course.href
          val miniature = course.localImg
          val screenshot = course.screenshot
          val oDomains = course.domain.map(d => List(new Domain(d)))
          val oSubdomains = course.subdomain.map(d => List(new Subdomain(d)))

          // Create the resource
          Resource(
            source = source,
            title = Title(title),
            oLevel = Some(level),
            oHref = Some(href),
            oMiniature = Some(miniature),
            oScreenshot = Some(screenshot),
            oDomains = oDomains,
            oSubdomains = oSubdomains,
            oTocs = oTocs,
            oDescriptions = oDescriptions
          )
        } else {
          throw new Exception("No description nor toc")
        }
      }
    }
  }
}
