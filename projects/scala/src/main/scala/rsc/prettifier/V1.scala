package rsc.prettifier

import rsc.attributes.{Pointer, PointerNameType}
import rsc.prettifier.lexer.Tokens._
import rsc.prettifier.lexer.{Eci, KeywordTokenKind, NumeralSystem}
import rsc.prettifier.structure.books._
import rsc.prettifier.structure.others.{Keyword, Numeration}
import rsc.prettifier.structure.{Structure, Unknown}
import rsc.toc.{Node, Toc}

class V1 {

  type Diagonal = List[(Int, Int)]
  case class ENode(struct: Structure, depth: Int, node: Node)

  def extract(s: String)
  : Structure =
    parser.Simple.process(lexer.Simple.process(s))

  def process(toc: Toc,
              forceKeywordMode: Boolean = false,
              forceBookMode: Boolean = false)
  : Toc =
    toc.nodesWithDepth(offset = 0) match {
      case Nil => toc
      case pairs =>
        // Extract the nodes
        val enodes = pairs.map {
          case (node, depth) =>
            ENode(extract(node.label), depth, node)
        }

        if(forceKeywordMode) {
          processKeywords(toc, enodes)
        } else if (forceBookMode) {
          processBook(toc, enodes)
        } else {
          processGuess(toc, enodes)
        }
  }

  private def processGuess(toc: Toc,
                   enodes: List[ENode]) = {
    /*
     * If there are no direct annotations like chapters, etc..
     * Check if there are numeral indications or keywords
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

    val keywordAnnotations = enodes.filter {
      case ENode(Keyword(_, _, _), _, _) => true
      case _ => false
    }

    (directAnnotations, numeralAnnotations, keywordAnnotations) match {
      case (Nil, Nil, Nil) =>
        processKeywords(toc, enodes)

      case (Nil, some, Nil) =>
        // If there are only numeral annotations, infer direct annotations
        processBook(toc, enodes)

      case (Nil, _, some) =>
        // If there are some keyword annotation and no direct annotations
        // Use them no matter if there is also any numeral annotations
        processKeywords(toc, enodes)

      case (some, _, _) =>
        // We have direct annotations! Use them
        processBook(toc, enodes)
    }
  }

  private def processKeywords(toc: Toc, _enodes: List[ENode])
  : Toc = {

    // Convert book "Part" annotation into the keyword "Part"..
    val enodes = _enodes.map {
      case ENode(Part(oNumer, oText), depth, node) =>
        ENode(Keyword(PARTKEYWORDKIND, oNumer, oText), depth, node)

      case x => x
    }

    // Define assignments between levels and keywordKinds
    // Kinds are elected by majority
    val keywordsAndLevels: List[(KeywordTokenKind, Int)] =
      enodes.flatMap {
        case ENode(Keyword(kind, _, _), level, _) => List((kind, level))
        case _ => Nil
      }

    val assignments: Map[Int, Option[KeywordTokenKind]] =
      (0 until toc.depth).map {
        case level =>
          keywordsAndLevels.filter(_._2 == level) match {
            case Nil => (level, None)
            case xs =>
              val groups = xs.groupBy(_._1).toList
              val best = groups.sortBy(-_._2.size).head
              (level, Some(best._1))
          }
      }.toMap

    // Prettify recursively
    toc.copy(nodes = prettifyKeywords(
      toc.nodes,
      assignments,
      enodes.map(e => (e.node, e)).toMap,
      None,
      "1",
      0
    ))
  }

  private def prettifyKeywords(nodes: List[Node],
                       assignments: Map[Int, Option[KeywordTokenKind]],
                       enodesMap: Map[Node, ENode],
                       oKind: Option[String],
                       num: String,
                       level: Int)
  : List[Node] = nodes match {
    case Nil => Nil
    case _ =>
      // Are we forced to use a defined kind?
      oKind match {
        case None =>
          // No
          // Do we have a kind assigned to this level?
          val oLevelKind = assignments(level).map {
            case WEEKKIND => "Week"
            case SESSIONKIND => "Session"
            case QUIZKIND => "Quiz"
            case EXERCISEKIND => "Exercise"
            case EXAMKIND => "Exam"
            case UNITKIND => "Unit"
            case PARTKEYWORDKIND => "Part"
            case _ => "Module"
          }

          nodes.foldLeft(num, false, List[Node]()) {
            case ((currentNum, initialized, acc), node) =>
              val enode = enodesMap(node)

              val decidedNum = oLevelKind match {
                case None => currentNum
                case Some(_) => assignments(level).map {
                  case tkt =>
                    val rescueNum = initialized match {
                      case false => "1"
                      case true => currentNum
                    }

                    enode.struct match {
                      case Keyword(tkt2, Some(x), _) => tkt == tkt2 match {
                        case true => x.toString
                        case false => rescueNum
                      }
                      case _ => rescueNum
                    }
                }.getOrElse(currentNum)
              }

              // Update the children
              val newChildren = prettifyKeywords(
                node.children,
                assignments,
                enodesMap,
                oLevelKind,
                s"$decidedNum.1",
                level + 1
              )

              // Update the current node
              val newNode = node.copy(children = newChildren, oPointer = Some(
                Pointer(
                  PointerNameType.Keyword,
                  s"${oLevelKind.getOrElse("Module")} $decidedNum",
                  extractText(enode.struct).getOrElse(""))
              ))

              // Produce the next num
              (Eci.succ(decidedNum), true, acc ::: List(newNode))
          }._3

        case Some(kind) =>
          // Yes, we should use this kind
          nodes.foldLeft(num, List[Node]()) {
            case ((currentNum, acc), node) =>
              val enode = enodesMap(node)

              // Update the children
              val newChildren = prettifyKeywords(
                node.children,
                assignments,
                enodesMap,
                oKind,
                s"$currentNum.1",
                level + 1
              )

              // Update the current node
              val newNode = node.copy(children = newChildren, oPointer = Some(
                Pointer(
                  PointerNameType.Keyword,
                  s"$kind $currentNum",
                  extractText(enode.struct).getOrElse(""))
              ))

              // Produce the next num
              (Eci.succ(currentNum), acc ::: List(newNode))
          }._2
      }
  }

  private def processBook(toc: Toc, _enodes: List[ENode])
  : Toc = {
    val enodes = _enodes.map {
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

    val pairs = enodes.map(e => (e.node, e.depth))

    // Process the toc
    val depthX = pairs.map(_._2).max + 1

    /*
     * -2: Numeration
     * -1: Unknown
     *
     * 0: Part
     * 1: Chapter
     * 2: Section
     */
    val elements = enodes.map {
      case ENode(Part(_, _), depth, _) =>
        (depth, 0)
      case ENode(Chapter(_, _), depth, _) =>
        (depth, 1)
      case ENode(Section(_, _), depth, _) =>
        (depth, 2)

      case ENode(Keyword(_, _, _), depth, _) =>
        (depth, -3)
      case ENode(Numeration(_, _, _), depth, _) =>
        (depth, -2)
      case ENode(Unknown(_), depth, _) =>
        (depth, -1)
    }

    // Count how many elements are on each diagonals
    val diagonals: List[Diagonal] = getDiagonals(depthX, 3) // Consider elements [0, 1, 2]

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

    // Select the best diagonal
    val diag = counts.sortBy {
      case (xs, count) => (-count, -xs.size)
    }.head match {
      case (_, 0) =>
        // In the case that there are no annotations
        // Take the full diagonal (the biggest one)
        diagonals.sortBy(-_.size).head

      case (d, _) => d
    }

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

    // Prettify the nodes
    val extractions = enodes.map {
      case ENode(struct, _, node) =>
        (node, struct)
    }.toMap

    val numeration = diag.sortBy(_._1).head match {
      case (_, y) => y match {
        case 0 => "i"
        case _ => "1"
      }
    }

    val (newNodes, _) = prettify(
      assignments,
      extractions,
      toc.nodes,
      0,
      numeration
    )
    toc.copy(nodes = newNodes)
  }

  private def prettify(assignments: Map[Int, PointerNameType.PointerName],
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
          (acc ::: List(newNode), newNum, oNewLastNum)
      }
      (newNodes, lastNumeration)
  }

  private def extractText(struct: Structure)
  : Option[String] = struct match {
    case Part(_, x) => x
    case Chapter(_, x) => x
    case Section(_, x) => x
    case Unknown(x) => Some(x)
    case Keyword(_, _, x) => x
    case Numeration(_, _, x) => x
  }

  private def instantiate(pointerType: PointerNameType.PointerName,
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

  private def getDiagonals(depthX: Int, depthY: Int)
  : List[Diagonal] = {

    val lineX = (0 until depthX).toList
    val lineY = (1 until depthY).toList
    val diagonalsX = lineX.map { case x => getDiagonal(x, 0, depthX, depthY) }
    val diagonalsY = lineY.map { case y => getDiagonal(0, y, depthX, depthY) }

    diagonalsX ::: diagonalsY
  }

  private def getDiagonal(startX: Int,
                          startY: Int,
                          depthX: Int,
                          depthY: Int,
                          acc: Diagonal = Nil)
  : Diagonal = startX < depthX match {
    case true =>
      getDiagonal(startX + 1, startY + 1, depthX, depthY, acc ::: List((startX, startY)))
    case false => acc
  }
}
