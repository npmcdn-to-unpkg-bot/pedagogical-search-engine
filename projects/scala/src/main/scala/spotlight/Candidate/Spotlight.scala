package spotlight.Candidate

import spotlight.Scores
import spotlight.Types.Types

case class Spotlight(label: String, uri: String, scores: Scores, types: Types) extends Candidate {
  override def toString(): String = s"Candidate: $label (uri: $uri)"
}
