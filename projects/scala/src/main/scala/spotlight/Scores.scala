package spotlight

case class Scores(contextualScore: Double,
                  percentageOfSecondRank: Double,
                  support: Int,
                  priorScore: Double,
                  finalScore: Double)
