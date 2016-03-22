package utils

import org.jsoup.nodes.Element

import _root_.scala.collection.JavaConverters._
import _root_.scala.collection.mutable
import _root_.scala.util.hashing.MurmurHash3
import _root_.java.lang.Iterable
import _root_.java.lang.Math.abs

object Conversions {
  def l[U](javaList: Iterable[U]): List[U] = javaList.iterator().asScala.toList

  def list2Option[A](xs: List[A]): Option[List[A]] = xs match {
    case Nil => None
    case _ => Some(xs)
  }

  def toBuffer[U](a: List[U]): mutable.Buffer[U] = mutable.Buffer().++(a)

  def normalize(s: String): String = s.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase.trim

  def textOf(s: String): String = s.
    replaceAll("\\u00a0", " ").
    replaceAll("\\u00ae", "").
    replaceAll("[^a-zA-Z0-9 !\"#$%&'()*+,\\-.\\/:;\\\\<=>\\]?@\\[\\^_`{|}~]", " "). // Keep only these characters
    replaceAll("\\s+", " "). // Compact spaces+ into " "
    trim

  def textOf(e: Element): String = textOf(e.text())

  def hash(s: String): String = MurmurHash3.stringHash(s) match {
    case n if n < 0 => "0" + abs(n)
    case p => "1" + p
  }
}
