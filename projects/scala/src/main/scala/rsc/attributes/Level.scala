package rsc.attributes

object Level extends Enumeration {
  type Level = Value

  val Fundamental = Value("fundamental")
  val HighSchool = Value("high-school")
  val University = Value("university")
  val Expert = Value("expert")
}
