package rsc.prettifier

import org.scalatest.{FlatSpec, Matchers}
import rsc.attributes.{Pointer, PointerNameType}
import rsc.toc.{Node, Toc}

class PrettifierTests extends FlatSpec with Matchers {

  val prettifier = new V1()

  "Partial toc 1" should "be recognized" in {
    val toc = new Toc(List(
      Node("Introduction", children = List(
        Node("Chapter 1. About us"),
        Node("Chapter 2. About the field")
      )),
      Node("Advanced topics", children = List(
        Node("Generic algorithms", children = List(
          Node("Sorting lists"),
          Node("Finding cliques")
        )),
        Node("Advanced data structures")
      ))
    ))

    val p1 = Pointer(PointerNameType.Chapter, "Chapter i.1", "About us")
    val p2 = Pointer(PointerNameType.Chapter, "Chapter i.2", "About the field")
    val p3 = Pointer(PointerNameType.Part, "Part i", "Introduction")
    val p4 = Pointer(PointerNameType.Part, "Part ii", "Advanced topics")
    val p5 = Pointer(PointerNameType.Chapter, "Chapter ii.1", "Generic algorithms")
    val p6 = Pointer(PointerNameType.Chapter, "Chapter ii.2", "Advanced data structures")
    val p7 = Pointer(PointerNameType.Section, "Section ii.1.1", "Sorting lists")
    val p8 = Pointer(PointerNameType.Section, "Section ii.1.2", "Finding cliques")

    val oNewToc = prettifier.processBook(toc)
    oNewToc.nonEmpty shouldBe true

    oNewToc match {
      case Some(newToc) =>
        val n1 = newToc.nodes
        n1.size shouldBe 2

        val n2 = n1.head.children
        n2.size shouldBe 2

        val n3 = n1(1).children
        n3.size shouldBe 2

        val n4 = n3.head.children
        n4.size shouldBe 2

        val t1 = n1.head.children.head
        val t2 = n1.head.children(1)
        val t3 = n1.head
        val t4 = n1(1)
        val t5 = n1(1).children.head
        val t6 = n1(1).children(1)
        val t7 = n1(1).children.head.children.head
        val t8 = n1(1).children.head.children(1)

        t1.oPointer shouldBe Some(p1)
        t2.oPointer shouldBe Some(p2)
        t3.oPointer shouldBe Some(p3)
        t4.oPointer shouldBe Some(p4)
        t5.oPointer shouldBe Some(p5)
        t6.oPointer shouldBe Some(p6)
        t7.oPointer shouldBe Some(p7)
        t8.oPointer shouldBe Some(p8)
    }
  }

  "Partial toc 2" should "be recognized" in {
    val toc = new Toc(List(
      Node("Introduction", children = List(
        Node("Part i. About us"),
        Node("Part ii. About the field")
      )),
      Node("Advanced topics", children = List(
        Node("Part iii Generic algorithms", children = List(
          Node("Sorting lists"),
          Node("Finding cliques")
        )),
        Node("Advanced data structures")
      ))
    ))

    val p1 = Pointer(PointerNameType.Part, "Part i", "About us")
    val p2 = Pointer(PointerNameType.Part, "Part ii", "About the field")
    val p3 = Pointer(PointerNameType.None, "", "Introduction")
    val p4 = Pointer(PointerNameType.None, "", "Advanced topics")
    val p5 = Pointer(PointerNameType.Part, "Part iii", "Generic algorithms")
    val p6 = Pointer(PointerNameType.Part, "Part iv", "Advanced data structures")
    val p7 = Pointer(PointerNameType.Chapter, "Chapter iii.1", "Sorting lists")
    val p8 = Pointer(PointerNameType.Chapter, "Chapter iii.2", "Finding cliques")

    val oNewToc = prettifier.processBook(toc)
    println(oNewToc.getOrElse(""))
    oNewToc.nonEmpty shouldBe true

    oNewToc match {
      case Some(newToc) =>
        val n1 = newToc.nodes
        n1.size shouldBe 2

        val n2 = n1.head.children
        n2.size shouldBe 2

        val n3 = n1(1).children
        n3.size shouldBe 2

        val n4 = n3.head.children
        n4.size shouldBe 2

        val t1 = n1.head.children.head
        val t2 = n1.head.children(1)
        val t3 = n1.head
        val t4 = n1(1)
        val t5 = n1(1).children.head
        val t6 = n1(1).children(1)
        val t7 = n1(1).children.head.children.head
        val t8 = n1(1).children.head.children(1)

        t1.oPointer shouldBe Some(p1)
        t2.oPointer shouldBe Some(p2)
        t3.oPointer shouldBe Some(p3)
        t4.oPointer shouldBe Some(p4)
        t5.oPointer shouldBe Some(p5)
        t6.oPointer shouldBe Some(p6)
        t7.oPointer shouldBe Some(p7)
        t8.oPointer shouldBe Some(p8)
    }
  }


  "Partial toc 3" should "be recognized" in {
    val toc = new Toc(List(
      Node("Introduction", children = List(
        Node("About us"),
        Node("About the field")
      )),
      Node("Advanced topics", children = List(
        Node("Generic algorithms", children = List(
          Node("Sorting lists", children = List(
            Node("Part 1: Descending"),
            Node("Ascending")
          )),
          Node("Finding cliques")
        )),
        Node("Advanced data structures")
      )),
      Node("Finally", children = List(
        Node("Discussion", children = List(
          Node("Controversies", children = List(
            Node("Part 1: Philosphy")
          )),
          Node("Compromises")
        ))
      ))
    ))

    val oNewToc = prettifier.processBook(toc)
    val expected = """| Introduction
| ..About us
| ..About the field
| Advanced topics
| ..Generic algorithms
| ....Sorting lists
| ......Part i: Descending
| ......Part ii: Ascending
| ....Finding cliques
| ..Advanced data structures
| Finally
| ..Discussion
| ....Controversies
| ......Part iii: Philosphy
| ....Compromises"""

    oNewToc.getOrElse("").toString shouldEqual expected
  }

  "Partial toc 4" should "be recognized" in {
    val toc = new Toc(List(
      Node("Introduction", children = List(
        Node("Section 1 About us"),
        Node("About the field")
      )),
      Node("Chapter 2: Advanced topics", children = List(
        Node("Generic algorithms", children = List(
          Node("Sorting lists", children = List(
            Node("Part Descending"),
            Node("Ascending")
          )),
          Node("Finding cliques")
        )),
        Node("Advanced data structures")
      )),
      Node("Chapter 3 Finally", children = List(
        Node("Discussion", children = List(
          Node("Controversies", children = List(
            Node("Part Philosphy")
          )),
          Node("Last Part Compromises")
        ))
      ))
    ))

    val oNewToc = prettifier.processBook(toc)
    val expected = """| Chapter 1: Introduction
| ..Section 1.1: About us
| ..Section 1.2: About the field
| Chapter 2: Advanced topics
| ..Section 2.1: Generic algorithms
| ....Section 2.1.1: Sorting lists
| ......Section 2.1.1.1: Descending
| ......Section 2.1.1.2: Ascending
| ....Section 2.1.2: Finding cliques
| ..Section 2.2: Advanced data structures
| Chapter 3: Finally
| ..Section 3.1: Discussion
| ....Section 3.1.1: Controversies
| ......Section 3.1.1.1: Philosphy
| ....Section 3.1.2: Last part compromises"""

    oNewToc.getOrElse("").toString shouldEqual expected
  }
}
