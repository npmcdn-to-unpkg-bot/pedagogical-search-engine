package rsc.toc

import rsc.Types.Nodes

case class Toc(nodes: Nodes) {
  def nodesWithDepth(): List[(Node, Int)] = nodes.flatMap(node => {
    (node, 0)::node.childrenWithDepth(1)
  })

  def nodesRec(): Nodes = nodesWithDepth().map(_._1)
}