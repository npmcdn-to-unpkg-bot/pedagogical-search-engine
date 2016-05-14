package rsc.toc

import rsc.Types.Nodes

case class Toc(nodes: Nodes) {
  def nodesWithDepth(offset: Int = 0): List[(Node, Int)] = nodes.flatMap(node => {
    (node, offset)::node.childrenWithDepth(offset + 1)
  })

  // Warning, it is expressed in number of levels
  def depth(): Int = nodes match {
    case Nil => 0
    case xs => xs.map(_.depth(1)).max
  }

  def nodesRec(): Nodes = nodesWithDepth().map(_._1)

  def rawString(): String = nodes.map(node => {
    node.rawString()
  }).mkString("\n")

  override def toString(): String = nodes.map(_.toString).mkString("\n")
}