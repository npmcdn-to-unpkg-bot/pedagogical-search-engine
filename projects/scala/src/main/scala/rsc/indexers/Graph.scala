package rsc.indexers

import graph.edges.unbiased.AttachedWeight
import graph.{DirectedGraph, Pagerank, Utils}
import mysql.GraphFactory
import rsc.Resource
import rsc.Types.Nodes
import rsc.attributes.Candidate.{Candidate, Spotlight}
import utils.Constants
import utils.Math._
import utils.Utils.mergeOptions2List

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class Graph(_ec: ExecutionContext,
            coreMaxSize: Int = 25,
            fizzFactor: Double = 5) {
  implicit val ec = _ec
  val seedValue = "seed-value"

  def index(r: Resource): Future[Option[Resource]] = {
    // Create the seed graph
    seedGraph(r) flatMap {
      case miniGraph =>
        // Extract the seeds
        val seeds = miniGraph.getNodes.asScala.map(node => {
          node.getNodeAttr(seedValue).asInstanceOf[Seed]
        }).toSet

        // Expand the graph
        val uris = seeds.map(_.candidate.uri)
        val futureExpanded = GraphFactory.follow2(uris, 10.5)

        futureExpanded map {
          case expanded =>
            // Index the title
            index(expanded, seeds) match {
              case Nil => None
              case indices =>
                // Index the table of contents
                val newOTocs = r.oTocs.map(tocs => {
                  tocs.map(toc => {
                    val newNodes = indexNodes(toc.nodes)(expanded, seeds)
                    toc.copy(nodes = newNodes)
                  })
                })

                // Create the new resource
                Some(
                  r.copy(
                    title = r.title.copy(oIndices = Some(new Indices(indices))),
                    oTocs = newOTocs,
                    oIndexer = Some(Indexer.Graph)
                  )
                )
            }
        }
    }
  }

  private def indexNodes(nodes: Nodes)(implicit digraph: DirectedGraph, seeds: Set[Seed])
  : Nodes = nodes match {
    case Nil => Nil
    case _ => nodes.map(node => {
      // Index children
      val newChildren = indexNodes(node.children)

      // Create and select the allowed seeds
      val allowed = seeds.map(_.candidate.uri)
      val candidateSeeds = getSeeds(List(node), 0)
      val filtered = candidateSeeds.filter(seed => {
        allowed.contains(seed.candidate.uri)
      })

      // Using pagerank
      val oIndices = index(digraph, filtered) match {
        case Nil => None
        case indices => Some(new Indices(indices))
      }

      // Create the new node
      node.copy(
        children = newChildren,
        oIndices = oIndices
      )
    })
  }

  private def index(digraph: DirectedGraph, seeds: Set[Seed])
  : List[Index] = seeds.size match {
    case zero if zero == 0 => Nil
    case _ =>
      // Create a mapping: uri -> seed
      val mapping = mergeSeeds(seeds)
      val nbSeeds = mapping.keySet.size

      // Run the pagerank algorithm
      val nWeight = new graph.nodes.biased.Uniform(digraph, mapping.keys.toList.asJava)
      val eWeight = new AttachedWeight(
        digraph,
        Constants.Graph.Edges.Attribute.completeWlm,
        Constants.Graph.Edges.Attribute.normalizedCwlm)

      Pagerank.weighted(digraph, nWeight, eWeight, 0.8)

      // Produce the indices scores
      val nbIndices = math.floor(nbSeeds.toDouble * this.fizzFactor)
      val topNodes = digraph.getNodes.asScala.toList
        .sortBy(-_.getScore).take(nbIndices.toInt)

      val r1 = rescaleD(topNodes.map(_.getScore.toDouble))
      val r2 = rescaleD(r1.map(s => math.exp(s)))
      val r3 = r2.map(s => s * (math.log(1 + math.log(1 + nbSeeds.toDouble)) + 0.2))
      val scores = topNodes.zip(r3).map {
        case (node, score) =>
          val nodeId = node.getId
          mapping.contains(nodeId) match {
            // Candidates score is proportional to depth
            case true =>
              val depth = mapping(nodeId).depth
              score / (depth.toDouble + 1)

            // Candidates not in the seeds are a bit penalized
            case false => score / 2
          }
      }

      // Produce the indices
      topNodes.zip(scores).map(p => {
        val node = p._1
        val score = p._2

        Index(node.getId, score)
      })
  }

  def seedGraph(r: Resource)
  : Future[DirectedGraph] = {
    // Build the graph
    val seeds = getSeeds(r)
    val mapping = mergeSeeds(seeds)
    val future = GraphFactory.follow1(mapping.keySet, 9.0)

    future map {
      case digraph =>
        // Attach the seed to each node
        digraph.getNodes.asScala.foreach(node => {
          val uri = node.getId
          node.addNodeAttr(seedValue, mapping(uri))
        })

        // Skim the graph
        def nodeValue(node: graph.nodes.Node): Int = {
          // Intuitive heuristic, more complex could be designed (Machine Learning?)
          node.totalNeighbors()
        }

        def bestCC(digraph: DirectedGraph)
        : Set[graph.nodes.Node] = {
          Utils.connectedComponents(digraph.getNodes.asScala) match {
            case Nil => Set()
            case ccs =>
              val sorted = ccs.toList.sortBy(g => -g.toList.map(n => nodeValue(n)).sum)
              sorted.head
          }
        }

        def filterMaxCC(directedGraph: DirectedGraph) = {
          val uris = bestCC(digraph).map(_.getId)
          digraph.removeNodesNotIn(uris.asJava)
        }

        def skimStep(directedGraph: DirectedGraph) = {
          // Remove lonely and dangling nodes
          digraph.removeNodes(2)

          // Take the largest Connected Component
          filterMaxCC(directedGraph)
        }

        def removeStep(directedGraph: DirectedGraph) = {
          val nodes = digraph.getNodes.asScala.toList
          val sorted = nodes.sortBy(node => {
            val seed = node.getNodeAttr(seedValue).asInstanceOf[Seed]
            // node with fewest priority (, degree) that leaves
            (-seed.priority, nodeValue(node))
          })
          val target = sorted.head.getId
          digraph.removeNode(target)
        }

        skimStep(digraph)
        while (digraph.nbNodes() > this.coreMaxSize) {
          removeStep(digraph)
          skimStep(digraph)
        }

        digraph
    }
  }

  def saveGraph(digraph: DirectedGraph, name: String): Unit = {
    digraph.toJSONFile(
      digraph.getIDs.asScala.toList.asJava,
      s"$name.json",
      Constants.Graph.Edges.Attribute.normalizedCwlm)
  }

  case class Seed(candidate: Candidate, priority: Int, depth: Int)

  def mergeSeeds(seeds: Set[Seed])
  : Map[String, Seed] = {
    // Lowest depth wins
    val groups = seeds.groupBy(_.candidate.uri)
    groups.map {
      case (uri, xs) => (uri, xs.toList.sortBy(_.depth).head)
    }
  }

  def getSeeds(nodes: Nodes, offset: Int): Set[Seed] = {
    // Extract sub-nodes and their depth
    val pairs = nodes.flatMap(node => {
      (node, offset)::node.childrenWithDepth(offset + 1)
    })

    // Extract the candidates
    val oSpots = pairs.map {
      case (node, depth) =>
        node.oSpots.map(spots => {
          spots.map(spot => (spot, depth))
        })
    }
    val spots = mergeOptions2List(oSpots: _*).flatten
    val candidates = spots.flatMap {
      case (spot, depth) => spot.candidates.map(c => (c, depth))
    }

    // Create the seeds
    val seeds = candidates.map {
      case (candidate, depth) => Seed(candidate, 2, depth)
    }
    seeds.toSet
  }

  def getSeeds(r: Resource): Set[Seed] = {

    def skimSpecial(seeds: Set[Seed])(threshold: Double)
    : Set[Seed] = seeds.filterNot(s => willBeSkimed(s.candidate, threshold))

    val thre = 0.5

    // Seeds from title
    val titleSeeds: Set[Seed] = skimSpecial {
      val spots = mergeOptions2List(r.title.oSpots).flatten
      val candidates = spots.flatMap(_.candidates)
      candidates.map(c => Seed(c, 1, 0)).toSet
    }(thre)

    // Seeds from table of contents
    val tocsSeeds: Set[Seed] = skimSpecial {
      val oSeeds = r.oTocs.map(tocs => {
        // Collect the nodes
        val nodes = tocs.flatMap(_.nodes)

        // Extract the seeds
        // Note: The root node has a depth of 1
        getSeeds(nodes, 1)
      })
      val seeds = mergeOptions2List(oSeeds)
      seeds.flatten.toSet
    }(thre)

    // Seeds from metadata
    val metaSeeds: Set[Seed] = skimSpecial {
      val spots = mergeOptions2List(
        r.oKeywords.map(ks => mergeOptions2List(ks.map(_.oSpots): _*).flatten),
        r.oCategories.map(cs => mergeOptions2List(cs.map(_.oSpots): _*).flatten),
        r.oDomains.map(os => mergeOptions2List(os.map(_.oSpots): _*).flatten),
        r.oSubdomains.map(ds => mergeOptions2List(ds.map(_.oSpots): _*).flatten)
      ).flatten
      val candidates = spots.flatMap(_.candidates)
      // .. have depth 2
      candidates.map(c => Seed(c, 3, 2)).toSet
    }(thre)

    // Seeds from descriptions
    val descrSeeds: Set[Seed] = skimSpecial {
      val spots = mergeOptions2List {
        r.oDescriptions.map(ds => mergeOptions2List(ds.map(_.oSpots): _*).flatten)
      }.flatten
      val candidates = spots.flatMap(_.candidates)
      // .. have depth 2
      candidates.map(c => Seed(c, 4, 2)).toSet
    }(thre)

    titleSeeds ++ tocsSeeds ++ metaSeeds ++ descrSeeds
  }

  def willBeSkimed(c: Candidate, threshold: Double): Boolean = c match {
    case Spotlight(_, _, scores, _) => scores.finalScore < threshold
  }
}
