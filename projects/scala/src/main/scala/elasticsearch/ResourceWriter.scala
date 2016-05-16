package elasticsearch

import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import rsc.Resource
import rsc.attributes.Source.Source
import rsc.indexers.Indices
import rsc.toc.Node
import utils.StringUtils

import scala.util.{Failure, Success, Try}

object ResourceWriter {
  def jsonResources(r: Resource): List[JObject] = {
    val title = titleResource(r) match {
      case Success(e) => List(e)
      case _ => Nil
    }

    // We can produce a elasticsearch (es) document for each node
    // Then a custom made query to es could return the best node for each resource.
    // However, since such advanced query is not yet made, we disable this option.
    val withNodes = false
    val nodes: List[JObject] = withNodes match {
      case true =>
        r.oTocs.map(tocs => {
          tocs.flatMap(toc => toc.nodesRec().flatMap(node => {
            nodeResource(r, node) match {
              case Success(e) => List(e)
              case Failure(e) => Nil
            }
          }))
        }).getOrElse(Nil)

      case false =>
        Nil
    }

    title:::nodes
  }

  private def getEntryId(oIndices: Option[Indices])
  : String = oIndices match {
    case Some(e) => e.entryId
    case None => StringUtils.uuid36()
  }

  private def getHref(r: Resource)
  : String = rsc.Utils.getUrl(r).getOrElse("")

  def titleResource(r: Resource)
  : Try[JObject] =
    Try {
      val entryId = getEntryId(r.title.oIndices)
      val title = r.title.label
      produceResource(title, r.source, title, bodyText(r), entryId, r.resourceId,
        getHref(r))
    }

  def nodeResource(r: Resource, node: Node)
  : Try[JObject] =
    Try {
      val entryId = getEntryId(r.title.oIndices)

      produceResource(r.title.label, r.source, node.bestLabel(),
        bodyText(r), entryId, r.resourceId, getHref(r))
    }

  def descriptionText(r: Resource)
  : String =
    r.oDescriptions.map(dess =>
      dess.map(des => des.text).mkString("\n")
    ).getOrElse("")

  def tocsText(r: Resource)
  : String =
    r.oTocs.map(tocs =>
      tocs.map(_.rawString()).mkString("\n")
    ).getOrElse("")

  def bodyText(r: Resource)
  : String =
    s"${tocsText(r)}\n${descriptionText(r)}"

  def produceResource(title: String,
                      source: Source,
                      header: String,
                      body: String,
                      entryId: String,
                      resourceId: String,
                      href: String)
  : JObject =
    ("title" -> title) ~ ("source" -> source.toString) ~
      ("header" -> header) ~ ("body" -> body) ~
      ("entryId" -> entryId) ~ ("resourceId" -> resourceId) ~
      ("href" -> href)

}
