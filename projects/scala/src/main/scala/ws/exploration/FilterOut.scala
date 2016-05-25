package ws.exploration

import java.sql.Timestamp

import ws.exploration.events.Messages

object FilterOut {
  /**
    * Filter out the runs that have requested to be forgotten
    * via the 'forget-user' and 'unforget-user' messages.
    *
    * e1, e2, e3
    * m(forget-user)
    * e4
    * m(unforget-user)
    * e5, e6
    * ------------------
    * Only (e5, e6) should be kept
    */
  def requested(runs: List[UserRun])
  : List[UserRun] = {
    val forgetLabel = "forget-user"
    val unforgetLabel = "unforget-user"

    runs.flatMap {
      case run@UserRun(events) =>
        val lastForget = events.lastIndexWhere {
          case Messages(_, _, `forgetLabel`, _, _) => true
          case _ => false
        }
        val lastUnforget = events.lastIndexWhere {
          case Messages(_, _, `unforgetLabel`, _, _) => true
          case _ => false
        }
        (lastForget, lastUnforget) match {
          case (-1, _) => List(run)
          case (i, -1) => Nil
          case (i, j) if j > i =>
            val cutted = events.slice(j + 1, events.size)
            val newRun = run.copy(ordered = cutted)
            List(newRun)

          case _ => Nil
        }
    }
  }

  def timeBetween(runs: List[UserRun], from: Timestamp, to: Timestamp)
  : List[UserRun] = {
    runs.filter {
      case run@UserRun(events) =>
        val times = events.map(_.timestamp().getTime)
        val begin = times.min
        val end = times.max
        begin > from.getTime && end < to.getTime
    }
  }
}
