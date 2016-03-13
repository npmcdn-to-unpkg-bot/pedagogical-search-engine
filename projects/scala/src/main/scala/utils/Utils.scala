package utils

object Utils {
  def mergeOptions[U](o1: Option[U], o2: Option[U], f: (U, U) => U): Option[U] = (o1, o2) match {
    case (None, x) => x
    case (x, None) => x
    case (Some(v1), Some(v2)) => Some(f(v1, v2))
  }
}
