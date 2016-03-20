package utils

object Math {
  def rescaleD(l: List[Double]): List[Double] = l match {
    case Nil => Nil
    case _ => l.max match {
      case zero if zero == 0 => l
      case max => l.map(_/max)
    }
  }

  def normalizeD(l: List[Double]): List[Double] = l match {
    case Nil => Nil
    case _ => l.sum match {
      case zero if zero == 0 => l
      case sum => l.map(_/sum)
    }
  }
}
