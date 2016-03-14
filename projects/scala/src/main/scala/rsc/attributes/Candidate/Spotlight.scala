package rsc.attributes.Candidate

import rsc.attributes.Scores

case class Spotlight(label: String, uri: String, scores: Scores, types: String) extends Candidate {
  override def toString(): String = s"Candidate: $label (uri: $uri)"
}
