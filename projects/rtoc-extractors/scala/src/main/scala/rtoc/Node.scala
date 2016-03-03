package rtoc

import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.JsonDSL._

class Node(label: String, children: List[Node]) {

  def json(): JObject = {
    val cs = children.map(c => c.json())
    ("label" -> label) ~ ("children" -> cs)
  }

  def prettyPrint(spaces: String): String = {
    val affix = children match {
      case Nil => ""
      case _ => "\n" + children.map(c => c.prettyPrint(s"$spaces..")).mkString("\n")
    }
    s"$spaces$label$affix"
  }

  override def toString(): String = prettyPrint("| ")
}
