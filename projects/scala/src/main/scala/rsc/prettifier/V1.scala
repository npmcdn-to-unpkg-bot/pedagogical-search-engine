package rsc.prettifier

import rsc.attributes.{Pointer, PointerNameType}
import rsc.prettifier.lexer.{Eci, NumeralSystem}
import rsc.prettifier.structure.books._
import rsc.prettifier.structure.others.Numeration
import rsc.prettifier.structure.{Structure, Unknown}
import rsc.toc.{Node, Toc}

class V1 {

  type Diagonal = List[(Int, Int)]
  case class ENode(struct: Structure, depth: Int, node: Node)

  def extract(s: String)
  : Structure =
    parser.Simple.process(lexer.Simple.process(s))

  def process(toc: Toc)
  : Toc = {
    toc.nodesWithDepth(offset = 0) match {
      case Nil => toc
      case pairs =>
        // Extract the nodes
        val enodes = pairs.map {
          case (node, depth) =>
            ENode(extract(node.label), depth, node)
        }

        /*
         * If there are no direct annotations like chapters, etc..
         * Check if there are numeral indications
         * e.g. 1. , 1.1, ..
         */
        val directAnnotations = enodes.filter {
          case ENode(Part(_, _), _, _) => true
          case ENode(Chapter(_, _), _, _) => true
          case ENode(Section(_, _), _, _) => true
          case _ => false
        }

        val numeralAnnotations = enodes.filter {
          case ENode(Numeration(_, _, _), _, _) => true
          case _ => false
        }

        (directAnnotations, numeralAnnotations) match {
          case (Nil, Nil) =>
            // todo: Test for other keywords
            ???

          case (Nil, _) =>
            // Infer direct annotations with the numeral ones
            val enodes2 = enodes.map {
              case ENode(Numeration(numbers, original, oText), depth, node) =>
                val newStruct = original.map(_.toLowerCase) match {
                  case NumeralSystem.romanlower()::xs => xs.size match {
                    case 0 => Part(Some(numbers.last), oText)
                    case 1 => Chapter(Some(numbers.last), oText)
                    case _ => Section(Some(numbers.last), oText)
                  }
                  case xs => xs.size match {
                    case 1 => Chapter(Some(numbers.last), oText)
                    case _ => Section(Some(numbers.last), oText)
                  }
                }
                ENode(newStruct, depth, node)

              case x => x
            }
            processBook(toc, enodes2)

          case _ =>
            // We have direct annotations!
            processBook(toc, enodes)
        }
    }
  }

  def processBook(toc: Toc, enodes: List[ENode])
  : Toc = {
    val pairs = enodes.map(e => (e.node, e.depth))

    // todo: delete
    println(s"enodes")
    enodes.map {
      case e => println((e.depth, e.struct))
    }

    // Process the toc
    val depthX = pairs.map(_._2).max + 1

    // todo: delete
    println(s"depth: $depthX")

    /*
     * -2: Numeration
     * -1: Unknown
     *
     * 0: Part
     * 1: Chapter
     * 2: Section
     */
    val elements = enodes.map {
      case ENode(Part(_, _), depth, node) =>
        (depth, 0)
      case ENode(Chapter(_, _), depth, node) =>
        (depth, 1)
      case ENode(Section(_, _), depth, node) =>
        (depth, 2)

      case ENode(Numeration(_, _, _), depth, node) =>
        (depth, -2)
      case ENode(Unknown(_), depth, node) =>
        (depth, -1)
    }


    // todo: delete
    println(s"elements: \n $elements")

    // Count how many elements are on each diagonals
    val diagonals = getDiagonals(depthX, 3) // Consider elements [0, 1, 2]


    // todo: delete
    println("diagonals")
    diagonals.foreach {
      case diagonal => println(diagonal)
    }

    val counts = diagonals.map {
      case diagonal =>
        val count = diagonal.map {
          case (x, y) =>
            val matching = elements.filter {
              case (x2, y2) => x == x2 && y == y2
            }
            matching.size
        }.sum
        (diagonal, count)
    }

    // todo: delete
    println("counts")
    counts.map(println)

    // Select the best diagonal
    val best = counts.sortBy {
      case (diag, count) => (-count, -diag.size)
    }.head

    // todo: delete
    println(s"best: $best")

    val diag = best._1

    // Create the assignments between depth and structures
    val assignments = (0 until depthX).map {
      case depth =>
        diag.filter { case (dx, _) => dx == depth } match {
          case Nil =>
            (0, PointerNameType.None)
          case (_, dy)::Nil =>
            dy match {
              case 0 => (depth, PointerNameType.Part)
              case 1 => (depth, PointerNameType.Chapter)
              case _ => (depth, PointerNameType.Section)
            }
        }
    }.toMap

    // todo: delete
    println(s"assignments")
    assignments.map(println)

    // Prettify the nodes
    val extractions = enodes.map {
      case ENode(struct, _, node) =>
        (node, struct)
    }.toMap

    // todo: delete
    println(s"extractions:")
    extractions.foreach {
      case (node, struct) =>
        println(s"${node.label}: $struct")
    }


    val numeration = diag.sortBy(_._1).head match {
      case (_, y) => y match {
        case 0 => "i"
        case _ => "1"
      }
    }

    // todo: delete
    println(s"numeration $numeration")

    val (newNodes, _) = prettify(
      assignments,
      extractions,
      toc.nodes,
      0,
      numeration
    )
    toc.copy(nodes = newNodes)
  }

  def prettify(assignments: Map[Int, PointerNameType.PointerName],
               extractions: Map[Node, Structure],
               _nodes: List[Node],
               level: Int,
               numeration: String
              )
  : (List[Node], Option[String]) = _nodes match {
    case Nil => (Nil, None)
    case nodes =>
      // Prettify all nodes (at this level)
      val assignment = assignments.contains(level) match {
        case true => assignments(level)
        case false => PointerNameType.None
      }
      val init: (List[Node], String, Option[String]) = (Nil, numeration, None)
      val (newNodes, _, lastNumeration) = nodes.foldLeft(init) {
        case ((acc, num, oLastNum), node) =>
          // Get the extracted structure
          val struct = extractions(node)

          // Prettify the children
          val numerationBelow = assignment match {
            case PointerNameType.None => num
            case PointerNameType.Part => "1"
            case _ => s"$num.1"
          }
          val (newChildren, oUpdatedNum) = prettify(
            assignments,
            extractions,
            node.children,
            level + 1,
            numerationBelow
          )

          val newNode = node.copy(oPointer = Some(
            instantiate(assignment, num, extractText(struct).map(_.capitalize))
          ), children = newChildren)

          val oNewLastNum = assignment match {
            case PointerNameType.None => oUpdatedNum match {
              case None => oLastNum
              case _ => oUpdatedNum
            }
            case _ => Some(num)
          }
          val newNum = oNewLastNum match {
            case None => num
            case Some(x) => Eci.succ(x)
          }
          println(s"newNode='${newNode.bestLabel()}', num=$num, newNum=$newNum, oNewLastNum=$oNewLastNum")
          (acc ::: List(newNode), newNum, oNewLastNum)
      }
      (newNodes, lastNumeration)
  }

  def extractText(struct: Structure)
  : Option[String] = struct match {
    case Part(_, x) => x
    case Chapter(_, x) => x
    case Section(_, x) => x
    case Unknown(x) => Some(x)
    case Numeration(_, _, x) => x
  }

  def instantiate(pointerType: PointerNameType.PointerName,
                  numeration: String,
                  oText: Option[String])
  : Pointer = {
    val prefix = pointerType match {
      case PointerNameType.None =>
        ""
      case PointerNameType.Part =>
        s"Part $numeration"
      case PointerNameType.Chapter =>
        s"Chapter $numeration"
      case PointerNameType.Section =>
        s"Section $numeration"
    }
    val text = oText.getOrElse("")
    Pointer(pointerType, prefix, text)
  }

  def getDiagonals(depthX: Int, depthY: Int)
  : List[Diagonal] = {

    def diagonal(startX: Int, startY: Int, acc: Diagonal = Nil)
    : Diagonal = startX < depthX match {
      case true =>
        diagonal(startX + 1, startY + 1, acc ::: List((startX, startY)))
      case false => acc
    }

    val lineX = (0 until depthX).toList
    val lineY = (1 until depthY).toList
    val diagonalsX = lineX.map { case x => diagonal(x, 0) }
    val diagonalsY = lineY.map { case y => diagonal(0, y) }

    diagonalsX ::: diagonalsY
  }
}
