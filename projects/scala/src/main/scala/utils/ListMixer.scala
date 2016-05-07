package utils

object ListMixer {
  def mixByFPreserveOrder[U, T](tieOrderFunction: U => T,
                                lss: Iterable[U]*)(implicit ordering: Ordering[T])
  : Iterable[U] = {
    val entries = lss.flatMap(_.zipWithIndex)
    val tied = entries.groupBy(_._2).toList.sortBy(_._1)
    tied.flatMap {
      case (_, group) => group.map(_._1).sortBy[T](tieOrderFunction)
    }
  }

  def mixWithPriority[U](lss: Iterable[U]*)
  : Iterable[U] = {
    val tuples = lss.map {
      case Nil => (Nil, Nil)
      case head::tail => (Seq(head), tail)
    }
    val topXs = tuples.flatMap {
      case (head, _) => head
    }
    val tailsXs = tuples.map {
      case (_, tail) => tail
    }

    // Stop condition
    tailsXs.flatten.isEmpty match {
      case true => topXs
      case false =>
        topXs ++ mixWithPriority(tailsXs.toList: _*).toSeq
    }
  }
}
