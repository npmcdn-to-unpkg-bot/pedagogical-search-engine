package rsc.khan

import rsc.Resource
import rsc.Resource._
import rsc.attributes.{Category, Level, Source}
import rsc.khan.Types.Course
import utils.Conversions.list2Option

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
    val oCategories = list2Option(categories.map(Category(_)))
    Resource(source, title,
      oHref = Some(href),
      oLevel = Some(level),
      oCategories = oCategories,
      oTocs = Some(toc::Nil))
  }
}
