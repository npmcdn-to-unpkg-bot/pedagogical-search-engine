package utils

object ListMixer {
  def mixByFPreserveOrder[U, T](f: U => T, lss: Iterable[U]*)(implicit ordering: Ordering[T])
  : Iterable[U] = {
    val entries = lss.flatMap(_.zipWithIndex)
    val tied = entries.groupBy(_._2).toList.sortBy(_._1)
    tied.flatMap {
      case (_, group) => group.map(_._1).sortBy[T](f)
    }
  }
}
