package rsc.importers

import mysql.slick.tables.{Details, Indices, Types}
import rsc.importers.Importer.{Importer, SlickMysql}
import rsc.toc.{Node, Toc}
import rsc.{Formatters, Resource, Utils}
import slick.driver.MySQLDriver.api._

import scala.concurrent.{ExecutionContext, Future}

class SlickMysql(db: Database) extends Formatters {

  def importResource(r: Resource, ec: ExecutionContext)
  : Future[Resource] = {
    // Create the slick "table-queries"
    val indicesTQ = TableQuery[Indices]
    val detailsTQ = TableQuery[Details]

    // Created the rows to be inserted
    val indices = titleIndices(r) ++ nodesIndices(r)
    val details = titleDetail(r) ++ nodesDetails(r)

    // Transactionally: If it fails, no write is performed at all
    // This way, at retry-time, no "duplicate-primary-key exception" will be thrown
    // We could also "insert-ignore" in mysql, but slick does not have this option [2016.04.18]
    val inserts = DBIO.seq(
      indicesTQ ++= indices,
      detailsTQ ++= details
    ).transactionally

    // Insert the rows
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
    r.title.oIndices match {
      case None => Nil
      case Some(indices) => indices.values.map {
        case index => (index.uri, indices.entryId, index.score, r.resourceId, r.source.toString)
      }
    }
  }

  def titleDetail(r: Resource)
  : Seq[Types.Details] =
    r.title.oIndices match {
      case None => Nil
      case Some(indices) =>
        // Collect the details
        val title = r.title.label
        val href = hrefDetail(r)
        val snippet = getSnippetText(r.title.oIndices)
        Seq((indices.entryId, title, href, snippet))
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
    case nodes => nodes.flatMap(nodeDetail(_, r))
  }

  def nodeDetail(node: Node, r: Resource)
  : Seq[Types.Details] = node.oIndices match {
    case None => Nil
    case Some(indices) =>
      val title = r.title.label
      val href = hrefDetail(r)
      val snippet = getSnippetText(node.oIndices)
      Seq((indices.entryId, title, href, snippet))
  }

  def nodesIndices(r: Resource)
  : Seq[Types.Indices] = r.oTocs match {
    case None => Nil
    case Some(tocs) => tocs.flatMap(tocIndices(_, r))
  }

  def tocIndices(toc: Toc, r: Resource)
  : Seq[Types.Indices] = toc.nodesRec() match {
    case Nil => Nil
    case nodes => nodes.flatMap(nodeIndices(_, r))
  }

  def nodeIndices(node: Node, r: Resource)
  : Seq[Types.Indices] = {
    node.oIndices match {
      case None => Nil
      case Some(indices) => indices.values.map {
        case index =>
          val uri = index.uri
          val score = index.score
          (uri, indices.entryId, score, r.resourceId, r.source.toString)
      }
    }
  }

  def hrefDetail(r: Resource): Option[String] = Utils.getUrl(r)
}
