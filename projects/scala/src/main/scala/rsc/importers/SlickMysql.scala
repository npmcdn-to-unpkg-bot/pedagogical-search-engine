package rsc.importers

import mysql.slick.tables.{Details, Indices, Types}
import rsc.{Formatters, Resource}
import rsc.attributes.Source._
import rsc.importers.Importer.{Importer, SlickMysql}
import rsc.toc.{Node, Toc}
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

class SlickMysql(_ec: ExecutionContext, db: Database) extends Formatters {

  implicit val ec: ExecutionContext = _ec

  def importResource(r: Resource)
  : Future[Resource] = {
    //
    val indicesTQ = TableQuery[Indices]
    val detailsTQ = TableQuery[Details]

    //
    val indices = titleIndices(r) ++ nodesIndices(r)
    val details = Seq(titleDetail(r)) ++ nodesDetails(r)

    //
    val inserts = DBIO.seq(
      indicesTQ ++= indices,
      detailsTQ ++= details
    )

    // todo: updates


    //
    db.run(inserts).map(_ => {
      // Write in the resource that we imported it
      val existingImporters = r.oImporters match {
        case None => Nil
        case Some(xs) => xs
      }
      val newOImporters: Option[List[Importer]] =
        Some(SlickMysql::existingImporters)
      r.copy(oImporters = newOImporters)
    })(ec)
  }

  def titleIndices(r: Resource)
  : Seq[Types.Indices] = {
    val entryId = "-1" // todo: fill the correct one
    r.title.oIndices match {
      case None => Nil
      case Some(indices) => indices.values.map {
        case index => (index.uri, entryId, index.score)
      }
    }
  }

  def titleDetail(r: Resource)
  : Types.Details = {
    // Collect the details
    val entryId = "-1" // todo: fill the correct one
    val title = r.title.label
    val typeVal = typeDetail(r)
    val href = hrefDetail(r)
    val snippet = getSnippetText(r.title.oIndices)
    (entryId, title, typeVal, href, snippet)
  }

  def getSnippetText(oIndices: Option[rsc.indexers.Indices])
  : String = oIndices match {
    case None => ""
    case Some(is) => is.oSnippet match {
      case None => ""
      case Some(snippet) => org.json4s.native.Serialization.write(snippet)
    }
  }

  def nodesDetails(r: Resource)
  : Seq[Types.Details] = r.oTocs match {
    case None => Nil
    case Some(tocs) => tocs.flatMap(tocDetails(_, r))
  }

  def tocDetails(toc: Toc, r: Resource)
  : Seq[Types.Details] = toc.nodesRec() match {
    case Nil => Nil
    case nodes => nodes.map(nodeDetail(_, r))
  }

  def nodeDetail(node: Node, r: Resource)
  : Types.Details = {
    val entryId = "-1" // todo: fill the correct one
    val title = r.title.label
    val typeVal = typeDetail(r)
    val href = hrefDetail(r)
    val snippet = getSnippetText(node.oIndices)
    (entryId, title, typeVal, href, snippet)
  }

  def nodesIndices(r: Resource)
  : Seq[Types.Indices] = r.oTocs match {
    case None => Nil
    case Some(tocs) => tocs.flatMap(tocIndices(_))
  }

  def tocIndices(toc: Toc)
  : Seq[Types.Indices] = toc.nodesRec() match {
    case Nil => Nil
    case nodes => nodes.flatMap(nodeIndices(_))
  }

  def nodeIndices(node: Node)
  : Seq[Types.Indices] = {
    val snippet = getSnippetText(node.oIndices)
    node.oIndices match {
      case None => Nil
      case Some(indices) => indices.values.map {
        case index => {
          val entryId = "-1" // todo: fill the correct one
          val uri = index.uri
          val score = index.score
          (uri, entryId, score)
        }
      }
    }
  }

  def typeDetail(r: Resource): String = r.source match {
    case Coursera => "Coursera"
    case Khan => "Khan Academy"
    case MIT => "MIT"
    case Safari => "Book"
    case Scholarpedia => "Scholarpedia"
  }

  def hrefDetail(r: Resource): Option[String] = r.oHref match {
    case None => None
    case Some(partialHref) => r.source match {
      case Coursera => Some(s"https://www.coursera.org$partialHref")
      case Khan => Some(s"https://www.khanacademy.org$partialHref")
      case MIT => Some(s"http://ocw.mit.edu$partialHref")
      case Safari => None
      case Scholarpedia => Some(s"http://www.scholarpedia.org$partialHref")
    }
  }
}
