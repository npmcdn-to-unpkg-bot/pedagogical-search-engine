package mysql

import java.util

import graph.DirectedGraph
import graph.nodes.Node
import utils.Constants

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object GraphFactory {
  def normalizeWlm(score: Double): Double = Math.min(score, 16.0)

  def follow1(uris: Set[String], minWLM: Double)(implicit ec: ExecutionContext)
  : Future[DirectedGraph] = {
    // Create the digraph
    val digraph: DirectedGraph = new DirectedGraph
    uris.foreach(uri => digraph.addNode(new Node(uri)))

    val query = QueriesUtils.read(
      Constants.Mysql.QueriesPath.queryOutLinks,
      util.Arrays.asList(QueriesUtils.escapeAndJoin(uris.asJava))
    )

    Future {
      // .. follow links once
      val co: java.sql.Connection = Constants.database.getConnection()
      val rs = co.createStatement().executeQuery(query)
      (co, rs)
    } map {
      case (co, rs) =>
        while (rs.next) {
          val a = rs.getString("A").toLowerCase
          val b = rs.getString("B").toLowerCase
          val score = normalizeWlm(rs.getDouble("Complete"))
          if (score > minWLM) {
            if (uris.contains(b)) {
              // Create the edge a -> b
              digraph.addEdge(a, b)
              digraph.getNode(a).addEdgeAttr(b, Constants.Graph.Edges.Attribute.completeWlm, score)
            }
          }
        }
        rs.close()
        co.close()
        digraph
    }
  }

  def follow2(uris: Set[String], minWlm: Double)(implicit ec: ExecutionContext)
  : Future[DirectedGraph] = {
    // Create the digraph
    val digraph: DirectedGraph = new DirectedGraph
    uris.foreach(uri => digraph.addNode(new Node(uri)))

    val query = QueriesUtils.read(
      Constants.Mysql.QueriesPath.queryOutLinks,
      util.Arrays.asList(QueriesUtils.escapeAndJoin(uris.asJava))
    )

    Future {
      // .. follow links once
      val co: java.sql.Connection = Constants.database.getConnection()
      val rs = co.createStatement().executeQuery(query)
      (co, rs)
    } map {
      case (co, rs) =>
        val newNodes = rs.next match {
          case false => Set()
          case true =>
            Iterator.continually {
              val a = rs.getString("A").toLowerCase
              val b = rs.getString("B").toLowerCase
              val score = normalizeWlm(rs.getDouble("Complete"))

              uris.contains(b) || score > minWlm match {
                case true =>
                  // Create the "b" node if it does not exist
                  val newNode = !digraph.contains(b) match {
                    case true =>
                      digraph.getOrCreate(b)
                      List(b)
                    case false => Nil
                  }

                  // Create the edge a -> b
                  val nodeA = digraph.getNode(a)
                  digraph.addEdge(a, b)
                  nodeA.addEdgeAttr(b, Constants.Graph.Edges.Attribute.completeWlm, score)

                  // Return newly created nodes
                  newNode

                case false => Nil
              }
            }.takeWhile(_ => rs.next).flatten.toSet
        }
        rs.close()
        co.close()

        // .. follow the links a second time
        val fromIds = QueriesUtils.escapeAndJoin(newNodes.toList.asJava)
        val toIds: String = QueriesUtils.escapeAndJoin((newNodes ++ uris).asJava)

        QueriesUtils.read(
          Constants.Mysql.QueriesPath.queryOutLinksRestricted,
          util.Arrays.asList(fromIds, toIds)
        )
    } map {
      case q =>
        val co: java.sql.Connection = Constants.database.getConnection()
        val rs = co.createStatement().executeQuery(q)
        (co, rs)
    } map {
      case (co, rs) =>
        while (rs.next) {
          val a = rs.getString("A").toLowerCase
          val b = rs.getString("B").toLowerCase
          val score = normalizeWlm(rs.getDouble("Complete"))
          val nodeA = digraph.getOrCreate(a)
          digraph.addEdge(a, b)
          nodeA.addEdgeAttr(b, Constants.Graph.Edges.Attribute.completeWlm, score)
        }
        rs.close()
        co.close()
        digraph
    }
  }
}
