package evaluation.manual.task

import evaluation.manual.json.{Annotation, TocNode}
import rsc.Resource
import rsc.annotators.Standard
import rsc.attributes.{Source, Title}
import rsc.indexers.Indices
import rsc.toc.{Node, Toc}
import spotlight.LazyWebService

import scala.concurrent.{ExecutionContext, Future}

class ResourceBased(_ec: ExecutionContext,
                    ws: LazyWebService,
                    indexFn: Resource => Future[Option[Resource]]) extends Indexer {

  implicit private val ec = _ec

  private def annotate(r: Resource)
  : Future[Resource] = Standard.annotate(r, ws)

  override def index(annotation: Annotation)
  : Future[Annotation] = {
    // Transform the annotation toc into a resource
    val (newNodes, indexToNode) = transformNode(annotation.toc)
    val nodeToIndex = indexToNode.toList.map(_.swap).toMap
    val toc = new Toc(newNodes)
    val resource = new Resource(
      source = Source.Safari,
      title = new Title(annotation.title),
      oTocs = Some(List(toc))
    )

    // Annotate the resource
    annotate(resource).flatMap(r2 => {
      // Index the resource
      indexFn(r2).map {
        case Some(indexed) =>
          // Create a correspondence between nodes of unannotated and annotated tocs
          val newToc = indexed.oTocs.get.head
          val corr = newToc.nodesRec().zip(toc.nodesRec()).flatMap {
            case (n1, n2) => nodeToIndex.contains(n2) match {
              case true => List((nodeToIndex(n2), n1.oIndices))
              case false => Nil
            }
          }.toMap

          // Copy the indexes into the annotation
          copyAnnotations(indexed, annotation, corr)

        case None =>
          // No indexes produces
          annotation
      }

    })
  }

  private def transformNode(nodes: List[TocNode])
  : (List[Node], Map[String, Node]) = {
    val indexWithDetails = nodes.map(node => {
      val (newChildren, corrChildren) = transformNode(node.children)
      (node.index, (new Node(label = node.content, children = newChildren), corrChildren))
    })

    val newNodes = indexWithDetails.map(_._2._1)
    val newCorr = indexWithDetails.flatMap {
      case (index, (node, corrChildren)) => (index, node) :: corrChildren.toList
    }.toMap

    (newNodes, newCorr)
  }

  private def copyAnnotations(indexed: Resource,
                              annotation: Annotation,
                              corr: Map[String, Option[Indices]])
  : Annotation = {
    val newElements = annotation.annotations.map(e => {
      val indexes = corr(e.index) match {
        case Some(indices) =>
          val ordered = indices.values.sortBy(-_.score)
          ordered.map(_.uri)
        case None => Nil
      }

      e.copy(indexes = Some(indexes))
    })

    annotation.copy(annotations = newElements)
  }
}
