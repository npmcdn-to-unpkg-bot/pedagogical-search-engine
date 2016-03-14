package rsc.toc

import rsc.Types.{Spots, Nodes}

case class Node(label: String, children: Nodes = Nil, oSpots: Option[Spots] = None) {

  def prettyPrint(spaces: String): String = {
    val affix = children match {
      case Nil => ""
      case _ => "\n" + children.map(c => c.prettyPrint(s"$spaces..")).mkString("\n")
    }
    s"$spaces$label$affix"
  }

  override def toString(): String = prettyPrint("| ")
}
