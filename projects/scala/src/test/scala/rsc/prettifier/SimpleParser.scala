package rsc.prettifier

import org.scalatest.{FlatSpec, Matchers}
import rsc.prettifier.lexer.Token
import rsc.prettifier.lexer.Tokens._
import rsc.prettifier.structure._
import rsc.prettifier.structure.books._

class SimpleParser extends FlatSpec with Matchers {
  val e = new Extractor()

  "Parts" should "be recognized" in {
    e.extract("Part 2 ...") shouldEqual Part(Some(2), Some("..."))
    e.extract("Part 3: ...") shouldEqual Part(Some(3), Some("..."))
    e.extract("Part 3 : ...") shouldEqual Part(Some(3), Some("..."))
    e.extract("Part 3 :...") shouldEqual Part(Some(3), Some("..."))
    e.extract("Part d ...") shouldEqual Part(Some(4), Some("..."))
    e.extract("Part 2.4 ...") shouldEqual Part(Some(4), Some("..."))
    e.extract("Part 2.4: ...") shouldEqual Part(Some(4), Some("..."))
    e.extract("Part ii: ...") shouldEqual Part(Some(2), Some("..."))
    e.extract("Part II: ...") shouldEqual Part(Some(2), Some("..."))
    e.extract("Part 3.II: ...") shouldEqual Part(Some(2), Some("..."))
    e.extract("Part ...") shouldEqual Part(None, Some("..."))
    e.extract("Part 3. ...") shouldEqual Part(Some(3), Some("..."))
    e.extract("Part3 ...") shouldEqual Part(Some(3), Some("..."))
    e.extract("Part3: ...") shouldEqual Part(Some(3), Some("..."))
    e.extract("Part3. ...") shouldEqual Part(Some(3), Some("..."))
    e.extract("Part1.2 ...") shouldEqual Part(Some(2), Some("..."))
    e.extract("Part1.2: blabla") shouldEqual Part(Some(2), Some("blabla"))
  }

  "Trash" should "not be recognized" in {
    e.extract("yooo") shouldEqual Unknown("yooo")
    e.extract("nice 1 buddy") shouldEqual Unknown("nice 1 buddy")
    e.extract("more on this chapter") shouldEqual Unknown("more on this chapter")
    e.extract("see section ii.3.1") shouldEqual Unknown("see section ii.3.1")
  }

  "Trim" should "work" in {
    val space = WHITESPACE(" ")
    val chapter = new Token(CHAPTER)
    val part = new Token(PART)

    val s1 = List(space)
    val s2 = List(space, space)
    val s3 = List(space, space, space)

    val c1 = List(chapter)
    val c2 = List(space, chapter)
    val c3 = List(chapter, space)
    val c4 = List(space, space, chapter, space)
    val c5 = List(space, chapter, space, space)

    val cp1 = List(chapter, part)
    val cwp2 = List(chapter, space, part)
    val cp3 = List(space, chapter, part)
    val cp4 = List(chapter, part, space)
    val cwp5 = List(space, chapter, space, part)
    val cwp6 = List(chapter, space, part, space)
    val cwp7 = List(space, chapter, space, part, space)
    val cwwp8 = List(space, chapter, space, space, part)
    val cwwp9 = List(space, space, chapter, space, space, part, space, space)

    parser.Simple.trim(s1) shouldEqual Nil
    parser.Simple.trim(s2) shouldEqual Nil
    parser.Simple.trim(s3) shouldEqual Nil

    parser.Simple.trim(c1) shouldEqual List(chapter)
    parser.Simple.trim(c2) shouldEqual List(chapter)
    parser.Simple.trim(c3) shouldEqual List(chapter)
    parser.Simple.trim(c4) shouldEqual List(chapter)
    parser.Simple.trim(c5) shouldEqual List(chapter)

    parser.Simple.trim(cp1) shouldEqual List(chapter, part)
    parser.Simple.trim(cwp2) shouldEqual List(chapter, space, part)
    parser.Simple.trim(cp3) shouldEqual List(chapter, part)
    parser.Simple.trim(cp4) shouldEqual List(chapter, part)
    parser.Simple.trim(cwp5) shouldEqual List(chapter, space, part)
    parser.Simple.trim(cwp6) shouldEqual List(chapter, space, part)
    parser.Simple.trim(cwp7) shouldEqual List(chapter, space, part)
    parser.Simple.trim(cwwp8) shouldEqual List(chapter, space, space, part)
    parser.Simple.trim(cwwp9) shouldEqual List(chapter, space, space, part)
  }

  "Merge whitespaces" should "work" in {
    val space = WHITESPACE(" ")
    val chapter = new Token(CHAPTER)
    val part = new Token(PART)

    val s1 = List(space)
    val s2 = List(space, space)
    val s3 = List(space, space, space)

    val cwp1 = List(chapter, space, part)
    val cwp2 = List(chapter, space, space, space, part)

    val scsp1 = List(space, chapter, space, space, space, part)
    val scsp2 = List(space, space, chapter, space, space, space, part)

    parser.Simple.mergeWhitespaces(s1) shouldEqual List(space)
    parser.Simple.mergeWhitespaces(s2) shouldEqual List(space)
    parser.Simple.mergeWhitespaces(s3) shouldEqual List(space)

    parser.Simple.mergeWhitespaces(cwp1) shouldEqual List(chapter, space, part)
    parser.Simple.mergeWhitespaces(cwp2) shouldEqual List(chapter, space, part)

    parser.Simple.mergeWhitespaces(scsp1) shouldEqual List(space, chapter, space, part)
    parser.Simple.mergeWhitespaces(scsp2) shouldEqual List(space, chapter, space, part)
  }
}
