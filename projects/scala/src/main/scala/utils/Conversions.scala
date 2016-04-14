package utils

import org.jsoup.nodes.Element

import _root_.scala.collection.JavaConverters._
import _root_.scala.collection.mutable
import _root_.java.lang.Iterable

object Conversions {
  def l[U](javaList: Iterable[U]): List[U] = javaList.iterator().asScala.toList

  def list2Option[A](xs: List[A]): Option[List[A]] = xs match {
    case Nil => None
    case _ => Some(xs)
  }

  def toBuffer[U](a: List[U]): mutable.Buffer[U] = mutable.Buffer().++(a)

  def textOf(e: Element): String = StringUtils.textOf(e.text())
}
