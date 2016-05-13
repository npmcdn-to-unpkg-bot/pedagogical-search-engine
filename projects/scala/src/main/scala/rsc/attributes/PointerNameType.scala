package rsc.attributes

object PointerNameType
  extends Enumeration {
  type PointerName = Value

  val None = Value("none")
  val Part = Value("part")
  val Chapter = Value("chapter")
  val Section = Value("section")
}
