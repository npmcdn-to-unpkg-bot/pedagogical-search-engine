package scholarpedia

import java.io.File

import Utils.Logger
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import rtoc.Data
import scholarpedia.Types.{Article, Downloaded}

class Articles(in: File) extends Data[Downloaded](in) {

  // Parse data
  implicit val formats = DefaultFormats
  val parsed = parse(in)

  // Extract each article
  val articles = parsed.extract[List[Article]]

  // Log articles without status
  articles.filter(a => a.status.isEmpty) match {
    case Nil => {}
    case xs => {
      val nb = xs.size
      Logger.warning(s"$nb do not have a status:")
      xs.map(a => Logger.warning(a.toString))
    }
  }

  // Filter downloaded articles
  val downloaded = articles.flatMap(a => a.page match {
    case None => Nil
    case Some(page) => List(Downloaded(a.label, a.href, page))
  })

  override def get(i: Int): Option[Downloaded] = (i < downloaded.size) match {
    case false => None
    case true => Some(downloaded(i))
  }

  override def mark(entry: Downloaded, name: String, xs: List[String]): Unit = {
    // todo: Implement
    val entryName = entry.label
    val values = xs.mkString
    Logger.info(s"$entryName marked with $name=$values")
  }

  override def flush(): Unit = {
    // todo: Implement
    Logger.info(s"Articles flushed")
  }
}
