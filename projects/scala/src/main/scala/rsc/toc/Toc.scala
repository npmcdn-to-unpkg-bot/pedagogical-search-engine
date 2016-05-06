package rsc.toc

import rsc.Types.Nodes

case class Toc(nodes: Nodes) {
  def nodesWithDepth(offset: Int = 0): List[(Node, Int)] = nodes.flatMap(node => {
    (node, offset)::node.childrenWithDepth(offset + 1)
  })

  def nodesRec(): Nodes = nodesWithDepth().map(_._1)

  def rawString(): String = nodes.map(node => {
    node.rawString()
  }).mkString("\n")
}