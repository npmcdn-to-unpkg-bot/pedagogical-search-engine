package utils

import scala.collection.mutable
import scala.collection.JavaConverters._

object Conversions {
  def l[U](javaList: java.util.List[U]): List[U] = javaList.iterator().asScala.toList

  def toBuffer[U](a: List[U]): mutable.Buffer[U] = mutable.Buffer().++(a)

  def normalize(s: String): String = s.replaceAll("[^a-zA-Z1-9 ]", "").toLowerCase.trim
}
