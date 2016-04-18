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
    case Nil => joinAcc.size match {
      case 0 => globAcc
      case _ => globAcc:::List(joinAcc)
    }
    case head::tail => {
      val joined = joinAcc.size match {
        case 0 => head
        case _ => joinAcc + joinStr + head
      }
      if(joined.size >= size) {
        glue(tail, size, joinStr, "", globAcc:::List(joined))
      } else {
        glue(tail, size, joinStr, joined, globAcc)
      }
    }
  }


  def normalize(s: String): String = s.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase.trim

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
