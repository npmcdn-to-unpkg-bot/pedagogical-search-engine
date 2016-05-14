package rsc.toc

import rsc.Types.{Nodes, Spots}
import rsc.attributes.{Pointer, PointerNameType}
import rsc.indexers.Indices

case class Node(label: String,
                children: Nodes = Nil,
                oSpots: Option[Spots] = None,
                oIndices: Option[Indices] = None,
                oPointer: Option[Pointer] = None) {

  // Warning, it is expressed in number of levels
  def depth(offset: Int = 1): Int = children match {
    case Nil => offset
    case xs => xs.map(_.depth(offset + 1)).max
  }

  def childrenRec(): Nodes = childrenWithDepth().map(_._1)

  def childrenWithDepth(offset: Int = 0): List[(Node, Int)] = {
    val current = children.map(c => (c, offset))
    val sub = children.flatMap(child => child.childrenWithDepth(offset + 1))
    current:::sub
  }

  def rawString(): String =
    (List(label):::children.map(c => c.rawString())).mkString("\n")

  def bestLabel(): String = oPointer match {
    case Some(Pointer(name, prefix, label)) =>
      name match {
        case PointerNameType.None =>
          label
        case _ =>
          s"$prefix: $label"
      }
    case None =>
      label
  }

  def prettyPrint(spaces: String): String = {
    val affix = children match {
      case Nil => ""
      case _ => "\n" + children.map(c => c.prettyPrint(s"$spaces..")).mkString("\n")
    }

    s"$spaces${bestLabel()}$affix"
  }

  def nSiblingsBefore(n: Int, node: Node): List[Node] = {
    // Find the node index
    val zipped = children.zipWithIndex
    zipped.filter(_._1.equals(node)) match {
      case Nil => Nil
      case x::_ => {
        // Find the node-indices to keep
        val index = x._2
        val a = math.max(0, index - n)
        val b = index - 1
        val toTake = (a to b)

        // Extract the nodes
        zipped.filter(z => toTake.contains(z._2)).map(_._1)
      }
    }
  }

  def nSiblingsAfter(n: Int, node: Node): List[Node] = {
    // Find the node index
    val zipped = children.zipWithIndex
    zipped.filter(_._1.equals(node)) match {
      case Nil => Nil
      case x::_ => {
        // Find the node-indices to keep
        val index = x._2
        val a = index + 1
        val b = math.min(children.size - 1, index + n)
        val toTake = (a to b)

        // Extract the nodes
        zipped.filter(z => toTake.contains(z._2)).map(_._1)
      }
    }
  }

  def nSiblingsAround(n: Int, node: Node): (List[Node], List[Node]) = {
    val before = n / 2
    val after = n - before
    val pre = nSiblingsBefore(before, node)

    // Did it achieved its goal?
    (pre.size == before) match {
      case true => {
        val post = nSiblingsAfter(after, node)
        // Did it achieved its goal?
        (post.size == after) match {
          case true => (pre, post)
          case false => {
            // Try to get a bit more before
            val newBefore = before + (after - post.size)
            (nSiblingsBefore(newBefore, node), post)
          }
        }
      }
      case false => {
        // try to get a bit more after
        val newAfter = after + (before - pre.size)
        (pre, nSiblingsAfter(newAfter, node))
      }
    }
  }

  override def toString(): String = prettyPrint("| ")
}
