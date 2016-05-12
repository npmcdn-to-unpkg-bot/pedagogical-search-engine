package rsc.prettifier.parser

import rsc.prettifier.lexer.Tokens._
import rsc.prettifier.lexer.{BookTokenKind, Token}
import rsc.prettifier.structure._
import rsc.prettifier.structure.books._

object Simple {
  private def mkText(tokens: List[Token]): String =
    tokens.map(_.rawString).mkString("")

  def instanciate(btk: BookTokenKind, oNumber: Option[Int], oText: Option[String])
  : Book = btk match {
    case PART => Part(oNumber, oText)
    case CHAPTER => Chapter(oNumber, oText)
    case SECTION => Section(oNumber, oText)
  }

  def process(tokens: List[Token])
  : Structure = {

    // Merge spaces
    val merged = mergeWhitespaces(tokens)
    val trimed = trim(merged)

    trimed match {

        /* Kinded, Whitespace, .. */
      case BookPart(bp)::WHITESPACE(_)::NUMERATION(ns, _)::SEPARATOR(":")::WHITESPACE(_)::xs =>
        // Part x: ...
        instanciate(bp, Some(ns.last), Some(mkText(xs)))

      case BookPart(bp)::WHITESPACE(_)::NUMERATION(ns, _)::WHITESPACE(_)::SEPARATOR(":")::WHITESPACE(_)::xs =>
        // Part x : ...
        instanciate(bp, Some(ns.last), Some(mkText(xs)))

      case BookPart(bp)::WHITESPACE(_)::NUMERATION(ns, _)::WHITESPACE(_)::SEPARATOR(":")::xs =>
        // Part x :...
        instanciate(bp, Some(ns.last), Some(mkText(xs)))

      case BookPart(bp)::WHITESPACE(_)::NUMERATION(ns, _)::WHITESPACE(_)::xs =>
        // Part x ...
        instanciate(bp, Some(ns.last), Some(mkText(xs)))

      case BookPart(bp)::WHITESPACE(_)::SEPARATOR(_)::xs =>
        // Part : ... Part , ... Part ; ... Part :...
        instanciate(bp, None, Some(mkText(trim(xs))))

      case BookPart(bp)::WHITESPACE(_)::xs =>
        // Part ...
        instanciate(bp, None, Some(mkText(xs)))


      /* Kinded, Numeration, .. */
      case BookPart(bp)::NUMERATION(ns, _)::WHITESPACE(_)::xs =>
        // Partx ...
        instanciate(bp, Some(ns.last), Some(mkText(xs)))

      case BookPart(bp)::NUMERATION(ns, _)::SEPARATOR(_)::WHITESPACE(_)::xs =>
        // Partx: ... Partx. ..
        instanciate(bp, Some(ns.last), Some(mkText(xs)))

      case BookPart(bp)::NUMERATION(ns, _)::SEPARATOR(_)::xs =>
        // Partx:...
        instanciate(bp, Some(ns.last), Some(mkText(xs)))


      /* Kinded, .. */
      case BookPart(bp)::SEPARATOR(_)::xs =>
        // Part: ... Part, ... Part; ... Part:...
        instanciate(bp, None, Some(mkText(trim(xs))))

      /* others */
      case xs =>
        println(xs)
        Unknown(mkText(xs))
    }
  }

  def mergeWhitespaces(tokens: List[Token])
  : List[Token] = tokens.foldLeft((List[Token](), false)) {
    case ((acc, wasSpace), token) => (token, wasSpace) match {
      case (WHITESPACE(_), true) => (acc, true)
      case (WHITESPACE(_), false) => (acc ::: List(token), true)
      case (_, _) => (acc ::: List(token), false)
    }
  }._1

  def trim(tokens: List[Token], onlyLeftSide: Boolean = false)
  : List[Token] = tokens match {
    case Nil => Nil
    case WHITESPACE(_)::Nil => Nil
    case token::Nil => List(token)
    case WHITESPACE(_)::tail => trim(tail, onlyLeftSide)
    case _ => onlyLeftSide match {
      case true => tokens
      case false => trim(tokens.reverse, onlyLeftSide = true).reverse
    }
  }
}
