package utils

object StringUtils {
  // with glue = 4
  // input: "a", "nice", "dog", "and", "a", "friendly", "cat"
  // output: "a nice", "dog and", "a friendly", "cat"
  def glue(chunks: List[String],
           size: Int = 4,
           joinStr: String = " ",
           joinAcc: String = "",
           globAcc: List[String] = Nil)
  : List[String] = chunks match {
    case Nil => joinAcc.size match {
      case 0 => globAcc
      case _ => globAcc:::List(joinAcc)
    }
    case head::tail => {
      val joined = joinAcc.size match {
        case 0 => head
        case _ => joinAcc + joinStr + head
      }
      if(joined.size >= size) {
        glue(tail, size, joinStr, "", globAcc:::List(joined))
      } else {
        glue(tail, size, joinStr, joined, globAcc)
      }
    }
  }
}
