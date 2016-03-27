package rsc.toc

import rsc.Types.{Nodes, Spots}
import rsc.indexers.Indices

case class Node(label: String,
                children: Nodes = Nil,
                oSpots: Option[Spots] = None,
                oIndices: Option[Indices] = None) {

  def childrenRec(): Nodes = childrenWithDepth().map(_._1)

  def childrenWithDepth(offset: Int = 0): List[(Node, Int)] = {
    val current = children.map(c => (c, offset))
    val sub = children.flatMap(child => child.childrenWithDepth(offset + 1))
    current:::sub
  }

  def prettyPrint(spaces: String): String = {
    val affix = children match {
      case Nil => ""
      case _ => "\n" + children.map(c => c.prettyPrint(s"$spaces..")).mkString("\n")
    }
    s"$spaces$label$affix"
  }

  override def toString(): String = prettyPrint("| ")
}
