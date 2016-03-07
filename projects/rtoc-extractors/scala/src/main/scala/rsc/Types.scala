package rsc

import org.json4s.JsonAST.JValue

object Types {
  // rsc types
  type Metadata = JValue
  type TOCs = List[TOC]
  type Descriptions = List[String]

  // other
  type ResourceElements = List[ResourceElement]
  type Nodes = List[Node]
  type Resources = List[Resource]
}
