package rsc.indexers

object Indexer extends Enumeration {
  type Indexer = Value

  val StandardSpotlight = Value("spotlight-0.7.2-wiki-20160113")
  val Graph = Value("graph")
  val GraphChoiceBased = Value("graph-choice-based")
}
