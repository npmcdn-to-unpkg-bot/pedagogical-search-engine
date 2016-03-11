package spotlight

import spotlight.Types.Candidates

case class Spot(text: String, interval: (Int, Int), candidates: Candidates) {
  override def toString(): String = {
    val candStr = candidates.map("  " + _.toString).mkString("\n")

    val margin = 15
    val start = Math.max(0, interval._1 - margin)
    val end = Math.min(text.length, interval._2 + margin)

    val marginStr = ".."
    val startPre = (interval._1 - margin > 0) match {
      case true => marginStr
      case false => ""
    }
    val endPost = (interval._2 + margin < text.length) match {
      case true => marginStr
      case false => ""
    }

    val subStr = text.substring(interval._1, interval._2)
    val preSubStr = text.substring(start, interval._1)
    val postSubStr = text.substring(interval._2, end)
    val preview = s"$startPre$preSubStr``$subStr``$postSubStr$endPost"
    s"Spot: $preview\n$candStr"
  }
}
