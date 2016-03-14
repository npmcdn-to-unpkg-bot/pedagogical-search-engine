package rsc

import java.io.File
import java.util.Date

import rsc.Types._
import rsc.attributes.Level.Level
import rsc.attributes.Source.Source
import rsc.attributes._
import utils.Conversions.{hash, normalize}

case class Resource(
                     // Metadata
                     source: Source,
                     title: Title,
                     oLevel: Option[Level] = None,
                     oAuthors: Option[List[String]] = None,
                     oPublishers: Option[List[String]] = None,
                     oPartners: Option[List[String]] = None,
                     oCreation: Option[Date] = None,
                     oUpdated: Option[Date] = None,
                     oKeywords: Option[Keywords] = None,
                     oCategories: Option[Categories] = None,
                     oHref: Option[String] = None,
                     oMiniature: Option[String] = None,
                     oScreenshot: Option[String] = None,
                     oDomains: Option[Domains] = None,
                     oSubdomains: Option[Subdomains] = None,

                     // TOCs
                     oTocs: Option[Tocs] = None,

                     // Descriptions
                     oDescriptions: Option[Descriptions] = None) {

  // Produce a human-friendly (unique) path to save the resource
  def friendlyPath: File = {
    val folder = source.toString

    // File name
    val shortHash = hash(java.util.UUID.randomUUID.toString)
    val nTitle = normalize(title.label)
    val prefix = nTitle.size match {
      case l if l > 30 => nTitle.substring(0, 31)
      case _ => nTitle
    }
    val name = s"$prefix-$shortHash"

    new File(s"$folder/$name")
  }
}

object Resource {
  // Some "implicit" conversions for ease of use of the case-class
  implicit def string2Title(label: String): Title =Title(label)
}
