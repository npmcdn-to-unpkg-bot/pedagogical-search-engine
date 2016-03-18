package rsc.toc

import rsc.Types.Nodes

case class Toc(nodes: Nodes) {
  def nodesRec(): Nodes = nodes.flatMap(node => node::node.childrenRec())
}