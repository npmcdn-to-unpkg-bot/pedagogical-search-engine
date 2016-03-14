package rsc.printers

import rsc.attributes.Spot

object Spots {
  def printSpot(spot: Spot, text: String): String = {
    // Generate text snippet
    val start = spot.start
    val end = spot.end
    val margin = 15
    val marginStr = ".."
    val preDots = (start - margin > 0) match {
      case true => marginStr
      case false => ""
    }
    val postDots = (end + margin < text.length) match {
      case true => marginStr
      case false => ""
    }

    val subStr = text.substring(start, end)
    val pre = text.substring(Math.max(0, start - margin), start)
    val post = text.substring(end, Math.min(text.length, end + margin))
    val snippet = s"$preDots$pre``$subStr``$post$postDots"

    // Add snippet to candidate-string
    val candStr = spot.candidates.map("  " + _.toString).mkString("\n")
    s"Spot: $snippet\n$candStr"
  }

  def printSpots(spots: Seq[Spot], text: String): String = spots.map(spot => printSpot(spot, text)).mkString("\n")
}
