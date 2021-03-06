package utils

import java.lang.Math._
import java.util.UUID

import scala.util.hashing.MurmurHash3

object StringUtils {
  // with glue = 4
  // input: "a", "nice", "dog", "and", "a", "friendly", "cat"
  // output: "a nice", "dog and", "a friendly", "cat"
  def glue(chunks: List[String],
           size: Int = 4,
           joinStr: String = " ",
           joinAcc: String = "",
           globAcc: List[String] = Nil)
  : List[String] = chunks match {
    case Nil => joinAcc.length match {
      case 0 => globAcc
      case _ => globAcc:::List(joinAcc)
    }
    case head::tail =>
      val joined = joinAcc.length match {
        case 0 => head
        case _ => joinAcc + joinStr + head
      }
      if(joined.length >= size) {
        glue(tail, size, joinStr, "", globAcc:::List(joined))
      } else {
        glue(tail, size, joinStr, joined, globAcc)
      }
  }

  def splitButKeep(s1: String, s2: String)
  : Array[String] =
    s1.split(s"""((?=$s2)|(?<=$s2))""")

  def normalize(s: String): String = s.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase.trim

  def normalizeUri(uri: String): String = uri.trim.toLowerCase

  def indicesOf(text: String, term: String)
  : List[Int] = {
    def rec(text: String, term: String, acc: List[Int], from: Int)
    : List[Int] = text.indexOf(term, from) match {
      case -1 => acc
      case i =>
        val stop = i + term.size
        rec(text, term, acc ::: List(i), stop)
    }
    rec(text, term, Nil, 0)
  }

  def escapeSQLWildcards(text: String): String =
    text.replaceAll("\\%", "\\\\%").replaceAll("\\_", "\\\\_")

  def textOf(s: String): String = s.
    replaceAll("\\u00a0", " ").
    replaceAll("\\u00ae", "").
    replaceAll("[^a-zA-Z0-9 !\"#$%&'()*+,\\-.\\/:;\\\\<=>\\]?@\\[\\^_`{|}~]", " "). // Keep only these characters
    replaceAll("\\s+", " "). // Compact spaces+ into " "
    trim

  def hash(s: String): String = MurmurHash3.stringHash(s) match {
    case n if n < 0 => "0" + abs(n)
    case p => "1" + p
  }

  def uuid36(): String = UUID.randomUUID().toString
}
