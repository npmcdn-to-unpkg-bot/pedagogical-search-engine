package rsc

import java.io.File
import java.util.Date

import org.json4s.native.Serialization._
import rsc.Types._
import rsc.annotators.Annotator.Annotator
import rsc.attributes.Level.Level
import rsc.attributes.Source.Source
import rsc.attributes._
import rsc.importers.Importer.Importer
import rsc.indexers.Index
import rsc.indexers.Indexer.Indexer
import rsc.prettifier.PrettifierType.Prettifier
import rsc.snippets.Snippetizer.Snippetizer
import utils.StringUtils.{hash, normalize}
import utils.StringUtils.uuid36

case class Resource(
                     // Metadata
                     resourceId: String = uuid36(),
                     oAnnotator: Option[Annotator] = None,
                     oIndexer: Option[Indexer] = None,
                     oSnippetizer: Option[Snippetizer] = None,
                     oPrettifier: Option[Prettifier] = None,
                     oImporters: Option[List[Importer]] = None,
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

  def topIndices(n: Int)
  : List[Index] = title.oIndices match {
    case None => Nil
    case Some(indices) => indices.values.sortBy(-_.score).take(n)
  }
}
