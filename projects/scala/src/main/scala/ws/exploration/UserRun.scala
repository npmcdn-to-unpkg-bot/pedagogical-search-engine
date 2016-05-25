package ws.exploration

import ws.exploration.events.{Clicks, Event, Messages, Searches}

case class UserRun(ordered: List[Event]) {
  override def toString(): String = {
    val title = "User run:"
    val events = ordered.map {
      case event =>
        val timestamp = event.timestamp()
        s"  $timestamp: $event"
    }.mkString("\n")
    s"$title\n$events"
  }
}

object UserRun {
  def generateFrom(clicks: List[Clicks],
                   messages: List[Messages],
                   searches: List[Searches])
  : List[UserRun] = {
    val gClicks = clicks.flatMap {
      case click => click.sid match {
        case None => Nil
        case Some(sid) => List((sid, click))
      }
    }
    val gMessages = messages.flatMap {
      case msg => msg.sid match {
        case None => Nil
        case Some(sid) => List((sid, msg))
      }
    }
    val gSearches = searches.flatMap {
      case search => search.sid match {
        case None => Nil
        case Some(sid) => List((sid, search))
      }
    }
    val grouped: Map[Int, List[(Int, Event)]] =
      (gClicks ::: gMessages ::: gSearches).groupBy {
        case (sid, x) => sid
      }

    grouped.map {
      case (sid, events) =>
        val ordered = events.sortBy {
          case (_, event) => event.timestamp().getTime
        }
        UserRun(ordered.map(_._2))
    }.toList
  }
}
