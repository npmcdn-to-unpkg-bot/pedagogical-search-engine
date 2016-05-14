package rsc.prettifier.parser

import rsc.prettifier.lexer.Tokens._
import rsc.prettifier.lexer.{BookTokenKind, Token}
import rsc.prettifier.structure._
import rsc.prettifier.structure.others._
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

      /* Numeration, .. */
      case NUMERATION(ns, os)::SEPARATOR(":")::WHITESPACE(_)::xs =>
        // 1: ..
        Numeration(ns, os, Some(mkText(xs)))

      case NUMERATION(ns, os)::WHITESPACE(_)::SEPARATOR(":")::WHITESPACE(_)::xs =>
        // 1 : ..
        Numeration(ns, os, Some(mkText(xs)))

      case NUMERATION(ns, os)::WHITESPACE(_)::SEPARATOR(":")::xs =>
        // 1 :..
        Numeration(ns, os, Some(mkText(xs)))

      case NUMERATION(ns, os)::WHITESPACE(_)::xs =>
        // 1 ..
        Numeration(ns, os, Some(mkText(xs)))

      /* Keywords */
      case KeywordToken(kt)::WHITESPACE(_)::NUMERATION(ns, os)::SEPARATOR(":")::WHITESPACE(_)::xs =>
        // Week 1: ..
        Keyword(kt, Some(ns.last), Some(mkText(xs)))

      case KeywordToken(kt)::WHITESPACE(_)::NUMERATION(ns, os)::WHITESPACE(_)::xs =>
        // Week 1 .., Week 1. ..
        Keyword(kt, Some(ns.last), Some(mkText(xs)))

      case KeywordToken(kt)::NUMERATION(ns, os)::SEPARATOR(":")::WHITESPACE(_)::xs =>
        // Week1: ..
        Keyword(kt, Some(ns.last), Some(mkText(xs)))

      case KeywordToken(kt)::NUMERATION(ns, os)::WHITESPACE(_)::xs =>
        // Week1 ..
        Keyword(kt, Some(ns.last), Some(mkText(xs)))

      /* others */
      case xs =>
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
