package ws.exploration.attributes

object Q3Type
extends Enumeration {
  type Q3Type = Value

  val Worse = Value("worse")
  val Equivalent = Value("equivalent")
  val Potential = Value("potential")
  val Better = Value("better")
}
