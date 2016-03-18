package agents

import java.io.File

import graph.Pagerank
import graph.edges.unbiased.AttachedWeight
import graph.nodes.Node
import mysql.GraphFactory
import org.json4s.native.JsonMethods._
import rsc.Types.Nodes
import rsc.attributes.Candidate.Spotlight
import rsc.{Formatters, Resource}
import rsc.indexers.Indexer
import utils.{Constants, Files, Logger, Settings}
import scala.collection.JavaConverters._
import scala.collection.immutable.HashSet

object IndexWithGraphs extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()

    // For each resource-file
    Files.explore(new File(settings.Resources.folder)).map(file => {
      // Parse it
      val json = parse(file.file)
      val r = json.extract[Resource]

      // Was it already indexed?
      val index = r.oIndexer match {
        case None => true
        case Some(indexer) => indexer match {
          case Indexer.Graph => false
          case _ => true
        }
      }


      val name = file.file.getAbsolutePath
      index match {
        case false => {
          Logger.info(s"Skipping: $name")
        }
        case true => {
          Logger.info(s"Processing ${file.file.getAbsolutePath}")

          val toc = r.oTocs match {
            case Some(tocs) => tocs.head
          }

          val candidates = toc.nodesRec().flatMap(_.oSpots match {
            case None => Nil
            case Some(spots) => spots.flatMap(spot => {
              spot.candidates.filter(candidate => candidate match {
                case Spotlight(_, _, scores, _) => (scores.finalScore > 0.5)
              })
            })
          })

          val uris = candidates.map(_.uri.toLowerCase)

          val digraph = GraphFactory.connect(uris.asJava)

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

          val nodes = digraph.getNodes.asScala
          val ccs = connectedComponents(nodes)
          val sorted = ccs.toList.sortBy(-_.size)
          val biggestCC = sorted.head
          val uris2 = biggestCC.map(_.getId)
          val digraph2 = GraphFactory.smart2(uris2.asJava)

          val nWeight = new graph.nodes.biased.Uniform(digraph2, uris2.asJava)
          val eWeight = new AttachedWeight(
            digraph2,
            Constants.Graph.Edges.Attribute.completeWlm,
            Constants.Graph.Edges.Attribute.normalizedCwlm)

          Pagerank.weighted(digraph2, nWeight, eWeight, 0.8)

          uris2.map(println(_))
          print(digraph2)

          digraph2.toJSONFile(candidates.map(_.uri).asJava,
            "test.json",
            Constants.Graph.Edges.Attribute.normalizedCwlm);
//          digraph.toJSONFile(candidates.map(_.uri).asJava,
//            "test.json",
//            Constants.Graph.Edges.Attribute.normalizedCwlm);

          // todo: remove
          System.exit(1)
        }
      }
    })
  }
}
