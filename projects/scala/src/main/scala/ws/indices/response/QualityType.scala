package ws.indices.response

object QualityType extends Enumeration {
  type Quality = Value

  val high = Value("high")
  val medium = Value("medium")
  val low = Value("low")
  val unknown = Value("unknown")

  def qualityFromScore(score: Double, size: Int)
  : Quality = size match {
    case 0 => QualityType.unknown
    case 1 => score match {
      case small if small < 1.0 => QualityType.low
      case medium if medium < 1.5=> QualityType.medium
      case high => QualityType.high
    }
    case 2 => (score / 2.0) match {
      case small if small < 0.6 => QualityType.low
      case medium if medium < 0.9=> QualityType.medium
      case high => QualityType.high
    }
    case _ => score match {
      case small if small < 1.5 => QualityType.low
      case medium if medium < 2.5 => QualityType.medium
      case high => QualityType.high
    }
  }
}
