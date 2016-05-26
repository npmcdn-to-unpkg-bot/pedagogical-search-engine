package ws.exploration.statistics

import ws.indices.indexentry.EngineType

class Printer(stat: Statistics) {
  def clickCount(): String = {
    val bing = stat.clickCount(EngineType.Bing)
    val wc = stat.clickCount(EngineType.Wikichimp)
    val wcft = stat.clickCount(EngineType.WikichimpFT)
    s"Count(clicks): Bing($bing), wc($wc), wcft($wcft)"
  }

  def usefulness(map: Map[EngineType.Engine, List[Int]]): String = {
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
        s"$engine(tot: $n, avg: $avg, var: $variance): $scoresAgg"
    }
    engines.mkString("\n")
  }

  def usefulnessAll(): String = {
    val content = usefulness(stat.usefulness())
    s"Usefulness for all votes\n$content"
  }

  def usefulnessSomeUri(): String = {
    val content = usefulness(stat.usefulnessSomeUri())
    s"Usefulness when there are at least 1 uri in the search terms:\n$content"
  }

  def usefulnessConcurence(): String = {
    val content = usefulness(stat.usefulnessConcurrency())
    s"Usefulness when there is both wc & bing results:\n$content"
  }

  def usefulnessComparison(): String = {
    val content = usefulness(stat.usefulnessComparison())
    s"Usefulness when there user voted on at least one wc & and one bing result:\n$content"
  }
}
