package ws.exploration.statistics

import ws.indices.indexentry.EngineType

class Printer(stat: Statistics) {
  def clickCount(): String = {
    val bing = stat.clickCount(EngineType.Bing)
    val wc = stat.clickCount(EngineType.Wikichimp)
    val wcft = stat.clickCount(EngineType.WikichimpFT)
    s"Count(clicks): Bing($bing), wc($wc), wcft($wcft)"
  }

  def usefulness(): String = {
    val map = stat.usefulness()
    val engines = map.toList.map {
      case (engine, scores) =>
        // Individual stats
        val scoresAgg = scores.groupBy(p => p).toList.sortBy(_._1).map {
          case (score, xs) =>
            val n = xs.size
            s"$score($n)"
        }.mkString(", ")

        // General stats
        val n = scores.size
        val sum = scores.sum
        val avg = sum.toDouble / n.toDouble
        val variance = scores.map(s => s.toDouble - avg).map(x => x * x).sum / (n - 1).toDouble

        //
        s"$engine(avg: $avg, var: $variance): $scoresAgg"
    }
    engines.mkString("\n")
  }
}
