package rsc

import org.json4s.JsonAST.JValue
import rsc.toc.{Toc, Node}

object Types {
  // rsc types
  type Metadata = JValue
  type Tocs = List[Toc]
  type Descriptions = List[String]

  // other
  type Nodes = List[Node]
  type Resources = List[Resource]
}
