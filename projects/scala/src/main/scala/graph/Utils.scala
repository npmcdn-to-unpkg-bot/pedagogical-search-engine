package graph

import graph.nodes.Node
import scala.collection.JavaConverters._

object Utils {
  def connectedComponents(nodes: Iterable[Node])
  : Iterable[Set[Node]] = {
    def follow(nodes: Iterable[Node],
               label: Int,
               seen: Set[Node])
    : Set[Node] = nodes.toList match {
      case Nil => seen
      case head::tail => {
        // Add the new node
        val node = head
        val candidates: Set[Node] = node.getIn.asScala.toSet ++ node.getOut.asScala
        val unseen: Set[Node] = candidates -- seen
        follow(unseen ++ tail, label, seen + node)
      }
    }

    def nextCC(nodes: Iterable[Node],
               acc: Map[Int, Set[Node]])
    : Map[Int, Set[Node]] = nodes.toList match {
      case Nil => acc
      case head::tail => {
        val label = acc.map(_._1) match {
          case Nil => 1
          case xs => xs.max + 1
        }
        val connected = follow(List(head), label, Set())
        val unseen = tail.filterNot(connected.contains(_))
        nextCC(unseen, acc + (label -> connected))
      }
    }

    nextCC(nodes, Map()).map(_._2)
  }
}
