package rsc.scholarpedia

import java.io.File

import rsc.extraction.Data
import rsc.scholarpedia.Types.{Article, ArticlesEntries}
import utils.Conversions.toBuffer

import scala.collection.mutable

class DataFile extends Data[Article] {

  // Extract each article
  val articleEntries = parsed.extract[ArticlesEntries]

  // Filter downloaded articles
  val articles = toBuffer(articleEntries.flatMap(a => a.page match {
    case None => Nil
    case Some(page) => Article(a.label, a.href, page, a.status)::Nil
  }))

  // Do no retry entries that previously failed
  override def passEntry(label: String): Boolean = (label.equals(notOk) || label.equals(ok))

  // Methods to implement
  override def data: mutable.Buffer[Article] = articles

  override def copy(o: Article, newStatus: String): Article = o.copy(status = Some(newStatus))

  override def in: File = new File(settings.Resources.Scholarpedia.data)
}
