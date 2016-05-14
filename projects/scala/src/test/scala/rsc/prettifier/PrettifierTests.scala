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

    val p1 = Pointer(PointerNameType.Chapter, "Chapter 1", "About us")
    val p2 = Pointer(PointerNameType.Chapter, "Chapter 2", "About the field")
    val p3 = Pointer(PointerNameType.Part, "Part i", "Introduction")
    val p4 = Pointer(PointerNameType.Part, "Part ii", "Advanced topics")
    val p5 = Pointer(PointerNameType.Chapter, "Chapter 1", "Generic algorithms")
    val p6 = Pointer(PointerNameType.Chapter, "Chapter 2", "Advanced data structures")
    val p7 = Pointer(PointerNameType.Section, "Section 1.1", "Sorting lists")
    val p8 = Pointer(PointerNameType.Section, "Section 1.2", "Finding cliques")

    val newToc = prettifier.process(toc)

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
    val p7 = Pointer(PointerNameType.Chapter, "Chapter 1", "Sorting lists")
    val p8 = Pointer(PointerNameType.Chapter, "Chapter 2", "Finding cliques")

    val newToc = prettifier.process(toc)
    println(newToc)

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

    val newToc = prettifier.process(toc)
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

    newToc.toString shouldEqual expected
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

    val newToc = prettifier.process(toc)
    val expected = """| Chapter 1: Introduction
| ..Section 1.1: About us
| ..Section 1.2: About the field
| Chapter 2: Advanced topics
| ..Section 2.1: Generic algorithms
| ....Section 2.1.1: Sorting lists
| ......Section 2.1.1.1: Part descending
| ......Section 2.1.1.2: Ascending
| ....Section 2.1.2: Finding cliques
| ..Section 2.2: Advanced data structures
| Chapter 3: Finally
| ..Section 3.1: Discussion
| ....Section 3.1.1: Controversies
| ......Section 3.1.1.1: Part philosphy
| ....Section 3.1.2: Last part compromises"""

    newToc.toString shouldEqual expected
  }


  "Partial toc 5" should "be recognized" in {
    val toc = new Toc(List(
      Node("1. Introduction", children = List(
        Node("About us"),
        Node("About the field")
      )),
      Node("2. Advanced topics", children = List(
        Node("Generic algorithms", children = List(
          Node("Sorting lists", children = List(
            Node("Descending"),
            Node("Ascending")
          )),
          Node("Finding cliques")
        )),
        Node("Advanced data structures")
      )),
      Node("C. Finally", children = List(
        Node("Discussion", children = List(
          Node("Controversies", children = List(
            Node("Philosphy")
          )),
          Node("Last Part Compromises")
        ))
      ))
    ))

    val newToc = prettifier.process(toc)
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

    newToc.toString shouldEqual expected
  }

  "Partial toc 6" should "be recognized" in {
    val toc = new Toc(List(
      Node("Introduction", children = List(
        Node("About us"),
        Node("About the field")
      )),
      Node("Advanced topics", children = List(
        Node("1. Generic algorithms", children = List(
          Node("1.1. Sorting lists", children = List(
            Node("Descending"),
            Node("Ascending")
          )),
          Node("Finding cliques")
        )),
        Node("1.2 Advanced data structures")
      )),
      Node("2. Finally", children = List(
        Node("2.1 Discussion", children = List(
          Node("Controversies", children = List(
            Node("Philosphy")
          )),
          Node("Last Part Compromises")
        ))
      ))
    ))

    val newToc = prettifier.process(toc)
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

    newToc.toString shouldEqual expected
  }

  "Partial toc 7" should "be recognized" in {
    val toc = new Toc(List(
      Node("Introduction", children = List(
        Node("About us"),
        Node("About the field")
      )),
      Node("Advanced topics", children = List(
        Node("i: eneric algorithms", children = List(
          Node(" Sorting lists", children = List(
            Node("Descending"),
            Node("Ascending")
          )),
          Node("Finding cliques")
        )),
        Node(" Advanced data structures")
      )),
      Node(" Finally", children = List(
        Node("ii: Discussion", children = List(
          Node("Controversies", children = List(
            Node("Philosphy")
          )),
          Node("Last Part Compromises")
        ))
      ))
    ))

    val newToc = prettifier.process(toc)
    val expected = """| Introduction
| ..Part i: About us
| ..Part ii: About the field
| Advanced topics
| ..Part iii: Eneric algorithms
| ....Chapter 1: Sorting lists
| ......Section 1.1: Descending
| ......Section 1.2: Ascending
| ....Chapter 2: Finding cliques
| ..Part iv: Advanced data structures
| Finally
| ..Part v: Discussion
| ....Chapter 1: Controversies
| ......Section 1.1: Philosphy
| ....Chapter 2: Last part compromises"""

    newToc.toString shouldEqual expected
  }

  "Partial toc 8" should "be recognized" in {
    val toc = new Toc(List(
      Node("Part i: Introduction", children = List(
        Node("About us"),
        Node("About the field")
      )),
      Node("Advanced topics", children = List(
        Node("i: eneric algorithms", children = List(
          Node(" Sorting lists", children = List(
            Node("Descending"),
            Node("Ascending")
          )),
          Node("Finding cliques")
        )),
        Node(" Advanced data structures")
      )),
      Node("Finally", children = List(
        Node("ii: Discussion", children = List(
          Node("Controversies", children = List(
            Node("Philosphy")
          )),
          Node("Last Part Compromises")
        ))
      ))
    ))

    val newToc = prettifier.process(toc)
    val expected = """| Part i: Introduction
| ..Chapter 1: About us
| ..Chapter 2: About the field
| Part ii: Advanced topics
| ..Chapter 1: Eneric algorithms
| ....Section 1.1: Sorting lists
| ......Section 1.1.1: Descending
| ......Section 1.1.2: Ascending
| ....Section 1.2: Finding cliques
| ..Chapter 2: Advanced data structures
| Part iii: Finally
| ..Chapter 1: Discussion
| ....Section 1.1: Controversies
| ......Section 1.1.1: Philosphy
| ....Section 1.2: Last part compromises"""

    newToc.toString shouldEqual expected
  }

  "Partial toc 9" should "be recognized" in {
    val toc = new Toc(List(
      Node("i: Introduction", children = List(
        Node("About us"),
        Node("About the field")
      )),
      Node("Advanced topics", children = List(
        Node("Chapter 1: eneric algorithms", children = List(
          Node(" Sorting lists", children = List(
            Node("Descending"),
            Node("Ascending")
          )),
          Node("Finding cliques")
        )),
        Node(" Advanced data structures")
      )),
      Node("Finally", children = List(
        Node("ii: Discussion", children = List(
          Node("Controversies", children = List(
            Node("Philosphy")
          )),
          Node("Last Part Compromises")
        ))
      ))
    ))

    val newToc = prettifier.process(toc)
    val expected = """| Part i: Introduction
| ..Chapter 1: About us
| ..Chapter 2: About the field
| Part ii: Advanced topics
| ..Chapter 1: Eneric algorithms
| ....Section 1.1: Sorting lists
| ......Section 1.1.1: Descending
| ......Section 1.1.2: Ascending
| ....Section 1.2: Finding cliques
| ..Chapter 2: Advanced data structures
| Part iii: Finally
| ..Chapter 1: Discussion
| ....Section 1.1: Controversies
| ......Section 1.1.1: Philosphy
| ....Section 1.2: Last part compromises"""

    newToc.toString shouldEqual expected
  }

  "Toc with keywords 1" should "be recognized" in {
    val toc = new Toc(List(
      Node("Week 1. Introduction", children = List(
        Node("i. About us"),
        Node("ii. About the field")
      )),
      Node("Week 2. Advanced topics", children = List(
        Node("1. Generic algorithms", children = List(
          Node("Sorting lists", children = List(
            Node("Descending"),
            Node("Ascending")
          )),
          Node("Finding cliques")
        )),
        Node("2. Advanced data structures")
      )),
      Node("Final words")
    ))

    val newToc = prettifier.process(toc)
    val expected = """| Week 1: introduction
| ..Week 1.1: about us
| ..Week 1.2: about the field
| Week 2: advanced topics
| ..Week 2.1: generic algorithms
| ....Week 2.1.1: sorting lists
| ......Week 2.1.1.1: descending
| ......Week 2.1.1.2: ascending
| ....Week 2.1.2: finding cliques
| ..Week 2.2: advanced data structures
| Week 3: final words"""

    newToc.toString shouldEqual expected
  }
}
