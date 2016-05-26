package ws.exploration

import java.sql.Timestamp

import org.json4s.native.Serialization._
import rsc.Formatters
import ws.exploration.attributes.{CategoryType, Q4Entry, Q4EntryFeedback, ResolvedQ4}
import ws.exploration.events.{Clicks, Event, Messages, Searches}
import ws.indices.spraythings.SearchTerm

case class UserRun(ordered: List[Event])
extends Formatters {
  override def toString: String = {
    val title = "User run:"
    val events = ordered.map {
      case event =>
        val timestamp = event.timestamp()
        s"  $timestamp: $event"
    }.mkString("\n")
    s"$title\n$events"
  }

  def q4View: String = {
    // Print one view for each search associated with any votes
    resolvedQ4.groupBy(_.search.searchlog).map {
      case (sameSearch, rq4s) =>
        // Produce the view with scores
        val search = rq4s.head.search
        val searchTerms = search.searchlog.searchTerms
        val filter = search.searchlog.filter
        val fromTo = (search.searchlog.from, search.searchlog.to)

        val scores = rq4s.map(rq4 => {
          (rq4.q4.entryId, rq4.q4.score)
        }).toMap
        val results = search.resultLog
        val lines = results.entries.map(entry => {
          val score = scores.contains(entry.entryId) match {
            case true =>
              val score = scores(entry.entryId)
              s"[$score] "
            case false => "[ ] "
          }
          val title = entry.title
          val engine = entry.engine.getOrElse("- no engine -")

          s"$score$title // $engine"
        }).mkString("\n")

        s"Searching:\n" +
          s"\tSearch terms: $searchTerms\n" +
          s"\tFilter, (from, to): $filter ($fromTo)\n" +
          s"View:\n" +
          s"$lines"

    }.mkString("\n\n")
  }

  lazy val resolvedQ4: List[ResolvedQ4] = {
    // Collect usefulness votes with times
    val tEntries = ordered.flatMap {
      case m @ Messages(_, _, CategoryType.Feedback, content, _) =>
        // There are two types of feedback, the simple string-valued
        // and the q4-valued
        try {
          val f = read[Q4EntryFeedback](content)
          List((m.timestamp(), f.value))
        } catch {
          case e: Throwable =>
            Nil
        }
      case _ => Nil
    }

    // Associate with the result entries
    val tuples = tEntries.flatMap {
      case (timestamp, q4 @ Q4Entry(_, searchLog, oSid, entryId)) =>
        val searchTerms = read[List[SearchTerm]](searchLog)
        val searchHash = SearchTerm.searchHash(searchTerms)
        UserRun.findSearch(this, searchHash, timestamp) match {
          case None => Nil
          case Some(search) =>
            search.resultLog.entries.filter(e => e.entryId.equals(entryId)) match {
              case Nil => Nil
              case xs => List(ResolvedQ4(q4, xs.head, search, timestamp))
            }
        }
      case _ => Nil
    }

    // Remove duplicates
    // e.g. a user rates several time the same entry
    val grouped = tuples.groupBy {
      case ResolvedQ4(q4, _, search, _) =>
        (q4.sid.getOrElse(-1), search.searchHash, q4.entryId)
    }
    val deduplicated = grouped.map {
      case ((_, xs)) => xs.sortBy(rq4 => -rq4.timestamp.getTime).head
    }

    //
    deduplicated.toList
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

  def findSearch(run: UserRun, searchHash: Int, timestamp: Timestamp)
  : Option[Searches] = {
    val xs = run.ordered.flatMap {
      case search @ Searches(_, `searchHash`, _, _, _, _) =>
        search.timestamp().before(timestamp) match {
          case true => List(search)
          case false => Nil
        }
      case _ => Nil
    }

    xs match {
      case Nil => None
      case _ => Some(xs.last)
    }
  }
}
