package ws.indices.spraythings

import rsc.attributes.Source
import rsc.attributes.Source.Source

object FilterParameterType
  extends Enumeration {
  type FilterParameter = Value

  val All = Value("all")
  val Free = Value("free")
  val Paid = Value("paid")

  def isSourceAllowed(filter: FilterParameter, source: Source)
  : Boolean = (filter, source) match {
    case (All, _) => true
    case (Free, Source.Safari) => false
    case (Free, _) => true
    case (Paid, Source.Safari) => true
    case (Paid, _) => false
  }
}
