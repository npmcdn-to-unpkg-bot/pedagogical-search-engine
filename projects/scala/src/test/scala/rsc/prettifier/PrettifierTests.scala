package rsc.prettifier

import org.scalatest.{FlatSpec, Matchers}
import rsc.toc.{Node, Toc}

class PrettifierTests extends FlatSpec with Matchers {

  val prettifier = new V1()

  "Partial toc 1" should "be recognized" in {
    val toc = new Toc(List(
      new Node("Introduction", children = List(
        new Node("Chapter 1. About us"),
        new Node("Chapter 2. About the field")
      )),
      new Node("Advanced topics", children = List(
        new Node("Generic algorithms", children = List(
          new Node("Sorting lists"),
          new Node("Finding cliques")
        )),
        new Node("Advanced data structures")
      ))
    ))
    val expected = new Toc(List(
      new Node("Introduction", children = List(
        new Node("Chapter 1. About us"),
        new Node("Chapter 2. About the field")
      )),
      new Node("Advanced topics", children = List(
        new Node("Generic algorithms", children = List(
          new Node("Sorting lists"),
          new Node("Finding cliques")
        )),
        new Node("Advanced data structures")
      ))
    ))

    val oNewToc = prettifier.processBook(toc)

    // todo: delete
    println("new toc")
    oNewToc match {
      case Some(newToc) =>
        println(newToc)
    }

  }

}
