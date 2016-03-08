package utils

import org.jsoup.nodes.Element

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.util.hashing.MurmurHash3

object Conversions {
  def l[U](javaList: java.util.List[U]): List[U] = javaList.iterator().asScala.toList

  def toBuffer[U](a: List[U]): mutable.Buffer[U] = mutable.Buffer().++(a)

  def normalize(s: String): String = s.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase.trim

  def textualize(s: String): String = s.replaceAll("\\u00a0", " ")

  def text(e: Element): String = textualize(e.text())

  def hash(s: String): String = MurmurHash3.stringHash(s) match {
    case n if n < 0 => "0" + n
    case p => "1" + p
  }
}
