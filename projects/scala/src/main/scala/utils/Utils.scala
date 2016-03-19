package utils

object Utils {
  def mergeOptions[U](o1: Option[U], o2: Option[U], f: (U, U) => U): Option[U] = (o1, o2) match {
    case (None, x) => x
    case (x, None) => x
    case (Some(v1), Some(v2)) => Some(f(v1, v2))
  }
  def mergeOptions2List[A](os: Option[A]*): List[A] = {
    def mergeRec(acc: List[A], os: Option[A]*): List[A] = os.toList match {
      case Nil => acc
      case head::tail => head match {
        case None => mergeRec(acc, tail: _*)
        case Some(e) => mergeRec(acc:::List(e), tail: _*)
      }
    }
    mergeRec(List(), os: _*)
  }
}
