package ws.exploration.statistics

import ws.exploration.attributes.Q3Type
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
    s"Usefulness when the user voted on at least one wc & and one bing result:\n$content"
  }

  def usefulnessBestSoftComparison(): String = {
    val content = usefulness(stat.usefulnessSoftBestComparison())
    s"Best(soft) usefulness when the user voted on at least one wc & and one bing result:\n$content"
  }

  def usefulnessBestHardComparison(): String = {
    val content = usefulness(stat.usefulnessBestHardComparison())
    s"Best(hard) usefulness when the user voted on at least one wc & and one bing result:\n$content"
  }

  def usefulnessWcHit(): String = {
    val content = usefulness(stat.usefulnessWcHit())
    s"Usefulness of results when there is at least on wc hit on the same page:\n$content"
  }

  def usefulnessClicked(): String = {
    val content = usefulness(stat.usefulnessClicked())
    s"Usefulness of results that were also clicked:\n$content"
  }

  def suggestionProportion(): String = {
    val proportion = stat.suggestionProportion()
    s"The proportion of searches with suggestions is $proportion"
  }

  def wcHitProportion(): String = {
    val proportion = stat.wcHitProportion()
    s"The proportion of researches with at least one wc results is $proportion"
  }

  def satisfactionQ3(grandMap: Map[Q3Type.Q3Type, Map[EngineType.Engine, Int]])
  : String = {
    val grandTot = grandMap.flatMap(_._2.values).sum

    // Order the map from "worse" to "better" for a nicer display
    val ordered = grandMap.toList.sortBy {
      case (Q3Type.Worse, _) => 1
      case (Q3Type.Equivalent, _) => 2
      case (Q3Type.Potential, _) => 3
      case _ => 4
    }

    val body = ordered.map {
      case (q3Value, map) =>
        val tot = map.values.sum
        val totRatio = utils.Math.round(tot.toDouble * 100.toDouble / grandTot.toDouble, 1)
        val header = s"$q3Value ($totRatio%, tot: $tot)"
        val lines = map.map {
          case (engine, count) =>
            val ratio = utils.Math.round(count.toDouble * 100.toDouble / tot.toDouble, 1)
            s"\t$engine($ratio%, tot: $count)"
        }.mkString("\n")
        s"$header\n$lines"
    }.mkString("\n\n")

    s"The satisfactions proportions are (tot: $grandTot):\n$body"
  }

  def satisfactionQ3All(): String = {
    val grandMap = stat.satisfactionQ3All()
    satisfactionQ3(grandMap)
  }

  def satisfactionQ3WithoutWCFT(): String = {
    val grandMap = stat.satisfactionQ3WithoutWCFT()
    satisfactionQ3(grandMap)
  }

  def satisfactionQ3WithoutWCFTInRun(): String = {
    val grandMap = stat.satisfactionQ3WithoutWCFTInRun()
    satisfactionQ3(grandMap)
  }

  def satisfactionQ3Clicks(): String = {
    val grandMap = stat.satisfactionQ3Click()
    val content = satisfactionQ3(grandMap)
    s"Clicks by satisfactions & engines:\n$content"
  }

  def satisfiedPeopleEntries(): String = {
    val pairs = stat.satisfiedPeopleEntries().toList
    val tot = pairs.map(_._2).sum

    val lines = pairs.map {
      case (engine, nb) =>
        val percentage = utils.Math.round(nb.toDouble * 100.toDouble / tot.toDouble, 1)
        s"$engine($nb): $percentage%"
    }.mkString("\n")

    s"Are satisfied people satisfied by which type of results?\n$lines"
  }

}
