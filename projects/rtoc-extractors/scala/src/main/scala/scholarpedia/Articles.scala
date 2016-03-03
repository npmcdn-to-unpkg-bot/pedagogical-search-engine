package scholarpedia

import java.io.{File, PrintWriter}

import Utils.Logger
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.writePretty
import rtoc.Data
import scholarpedia.Types.{Article, Downloaded}

import scala.collection.mutable

class Articles(in: File) extends Data[Downloaded](in) {

  // Parse data
  implicit val formats = DefaultFormats
  val parsed = parse(in)

  // Extract each article
  val articles = parsed.extract[List[Article]]

  // Filter downloaded articles
  val downloaded = mutable.Buffer[Downloaded]()
  articles.map(a => a.page match {
    case None => {}
    case Some(page) => downloaded.+=(Downloaded(a.label, a.href, page, a.status))
  })

  override def get(i: Int): Option[Downloaded] = (i < downloaded.size && i > -1) match {
    case false => None
    case true => Some(downloaded(i))
  }

  override def mark(entry: Downloaded, s: String): Unit = apply(entry, i => {
    downloaded(i) = downloaded(i).copy(status = Some(s))
  })

  override def apply[V](entry: Downloaded, f: Int => V): V = downloaded.indexOf(entry) match {
    case -1 => ???
    case index => f(index)
  }

  override def executeFlush(): Unit = {
    // Serialize data
    val c = writePretty(downloaded)
    val pw = new PrintWriter(in)

    // Write
    pw.write(c)
    pw.close()
    Logger.info(s"Articles flushed")
  }

  override def getMark(entry: Downloaded): Option[String] = apply(entry, i => downloaded(i).status)
}
