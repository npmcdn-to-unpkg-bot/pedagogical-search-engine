package rsc.prettifier

import org.scalatest.{FlatSpec, Matchers}
import rsc.prettifier.lexer.{BookTokenKind, Token}
import rsc.prettifier.lexer.Tokens._
import rsc.prettifier.structure._
import rsc.prettifier.structure.books._
import rsc.prettifier.structure.others.Numeration

class SimpleParser extends FlatSpec with Matchers {
  val e = new Extractor()
  val sampleText = "blabla"
  def getSamples(tag: String, btk: BookTokenKind)
  : Map[String, Book] = List(
    (s"$tag 2 $sampleText", parser.Simple.instanciate(btk, Some(2), Some(sampleText))),
    (s"$tag 2.3. $sampleText", parser.Simple.instanciate(btk, Some(3), Some(sampleText))),
    (s"$tag 3: $sampleText", parser.Simple.instanciate(btk, Some(3), Some(sampleText))),
    (s"$tag 3 : $sampleText", parser.Simple.instanciate(btk, Some(3), Some(sampleText))),
    (s"$tag 3 :$sampleText", parser.Simple.instanciate(btk, Some(3), Some(sampleText))),
    (s"$tag d $sampleText", parser.Simple.instanciate(btk, Some(4), Some(sampleText))),
    (s"$tag 2.4 $sampleText", parser.Simple.instanciate(btk, Some(4), Some(sampleText))),
    (s"$tag 2.4: $sampleText", parser.Simple.instanciate(btk, Some(4), Some(sampleText))),
    (s"$tag ii: $sampleText", parser.Simple.instanciate(btk, Some(2), Some(sampleText))),
    (s"$tag II: $sampleText", parser.Simple.instanciate(btk, Some(2), Some(sampleText))),
    (s"$tag 3.II: $sampleText", parser.Simple.instanciate(btk, Some(2), Some(sampleText))),
    (s"$tag $sampleText", parser.Simple.instanciate(btk, None, Some(sampleText))),
    (s"$tag 3. $sampleText", parser.Simple.instanciate(btk, Some(3), Some(sampleText))),
    (s"${tag}3 $sampleText", parser.Simple.instanciate(btk, Some(3), Some(sampleText))),
    (s"${tag}3: $sampleText", parser.Simple.instanciate(btk, Some(3), Some(sampleText))),
    (s"${tag}3. $sampleText", parser.Simple.instanciate(btk, Some(3), Some(sampleText))),
    (s"${tag}1.2 $sampleText", parser.Simple.instanciate(btk, Some(2), Some(sampleText))),
    (s"${tag}1.2: $sampleText", parser.Simple.instanciate(btk, Some(2), Some(sampleText)))
  ).toMap

  "Parts" should "be recognized" in {
    val samples = getSamples("Part", PART)

    samples.foreach {
      case (input, result) =>
        e.extract(input) shouldEqual result
    }
  }

  "Chapters" should "be recognized" in {
    val samples = getSamples("ChApTeR", CHAPTER)

    samples.foreach {
      case (input, result) =>
        e.extract(input) shouldEqual result
    }
  }

  "Sections" should "be recognized" in {
    val samples = getSamples("section", SECTION)

    samples.foreach {
      case (input, result) =>
        e.extract(input) shouldEqual result
    }
  }

  "Numerations" should "be recognized" in {
    val text = "hello"
    e.extract(s"1 $text") shouldEqual new Numeration(List(1), List("1"), Some(text))
    e.extract(s"1. $text") shouldEqual new Numeration(List(1), List("1"), Some(text))
    e.extract(s"1.ii. $text") shouldEqual new Numeration(List(1, 2), List("1", "ii"), Some(text))
    e.extract(s"1.1 $text") shouldEqual new Numeration(List(1, 1), List("1", "1"), Some(text))
    e.extract(s"a.1 $text") shouldEqual new Numeration(List(1, 1), List("a", "1"), Some(text))
    e.extract(s" ii.c.4 $text") shouldEqual new Numeration(List(2, 3, 4), List("ii", "c", "4"), Some(text))
    e.extract(s" \tii.c.4: $text") shouldEqual new Numeration(List(2, 3, 4), List("ii", "c", "4"), Some(text))
    e.extract(s"1.2.: $text") shouldEqual new Numeration(List(1, 2), List("1", "2"), Some(text))
    e.extract(s"1.2. : $text") shouldEqual new Numeration(List(1, 2), List("1", "2"), Some(text))

    e.extract(s"a small dog") shouldEqual new Numeration(List(1), List("a"), Some("small dog"))
    e.extract(s"a. small dog") shouldEqual new Numeration(List(1), List("a"), Some("small dog"))
    e.extract(s"a: small dog") shouldEqual new Numeration(List(1), List("a"), Some("small dog"))
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
