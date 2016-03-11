package spotlight.Candidate

import spotlight.Types.Types

case class Spotlight(label: String, uri: String, scores: Scores, types: Types) extends Candidate {
  override def toString(): String = s"Candidate: $label (uri: $uri)"
}

case class Scores(contextualScore: Double,
                 percentageOfSecondRank: Double,
                 support: Int,
                 priorScore: Double,
                 finalScore: Double)
