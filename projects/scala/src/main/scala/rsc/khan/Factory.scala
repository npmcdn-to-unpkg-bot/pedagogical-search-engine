package rsc.khan

import rsc.Resource
import rsc.attributes.{Level, Source}
import rsc.khan.Types.Course

class Factory extends rsc.extraction.Factory[Course] {
  override def getOrFail(course: Course): Resource = {
    // Open course
    val doc = open(course.path)

    // TOC
    val toc = doc match {
      case layouts.toc.Normal(re) => re
    }

    // Metadata
    val m1 = doc match {
      case layouts.metadata.Normal(re) => re
    }
    val title = m1.title
    val href = m1.href

    val source = Source.Khan
    val level = Level.HighSchool
    val categories = course.parents

    // Create resource
    Resource(source, title,
      oHref = Some(href),
      oLevel = Some(level),
      oCategories = Some(categories),
      oTocs = Some(toc::Nil))
  }
}
