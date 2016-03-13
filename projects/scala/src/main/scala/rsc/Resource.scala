package rsc

import java.io.File
import java.util.Date

import rsc.Types.{Descriptions, Tocs}
import rsc.attributes.Level.Level
import rsc.attributes.Source.Source
import utils.Conversions.{hash, normalize}

case class Resource(
                     // Metadata
                     source: Source,
                     title: String,
                     oLevel: Option[Level] = None,
                     oAuthors: Option[List[String]] = None,
                     oPublishers: Option[List[String]] = None,
                     oPartners: Option[List[String]] = None,
                     oCreation: Option[Date] = None,
                     oUpdated: Option[Date] = None,
                     oKeywords: Option[List[String]] = None,
                     oCategories: Option[List[String]] = None,
                     oHref: Option[String] = None,
                     oMiniature: Option[String] = None,
                     oScreenshot: Option[String] = None,
                     oDomain: Option[String] = None,
                     oSubdomain: Option[String] = None,

                     // TOCs
                     oTocs: Option[Tocs] = None,

                     // Descriptions
                     oDescriptions: Option[Descriptions] = None) {

  // Produce a human-friendly (unique) path to save the resource
  def friendlyPath: File = {
    val folder = source.toString

    // File name
    val shortHash = hash(java.util.UUID.randomUUID.toString)
    val nTitle = normalize(title)
    val prefix = nTitle.size match {
      case l if l > 30 => nTitle.substring(0, 31)
      case _ => nTitle
    }
    val name = s"$prefix-$shortHash"

    new File(s"$folder/$name")
  }
}
