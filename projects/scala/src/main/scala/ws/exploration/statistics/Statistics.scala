package ws.exploration.statistics

import org.json4s.native.Serialization.read
import rsc.Formatters
import ws.exploration.UserRun
import ws.exploration.attributes._
import ws.exploration.events.{Clicks, Messages, Searches}
import ws.indices.indexentry.EngineType.Engine
import ws.indices.spraythings.SearchTerm

class Statistics(runs: List[UserRun],
                 clicks: List[Clicks],
                 messages: List[Messages],
                 searches: List[Searches]) extends Formatters {

  private lazy val searchMap
  : Map[Int, Searches] = searches.map {
    case search => (search.searchHash, search)
  }.toMap

  private lazy val q4Map
  : Map[UserRun, List[(Q4Entry, Entry, Searches)]] = {
    runs.map {
      case run =>
        // Collect usefulness votes with times
        val tEntries = run.ordered.flatMap {
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
            UserRun.findSearch(run, searchHash, timestamp) match {
              case None => Nil
              case Some(search) =>
                search.resultLog.entries.filter(e => e.entryId.equals(entryId)) match {
                  case Nil => Nil
                  case xs => List((q4, xs.head, search))
                }
            }
          case _ => Nil
        }

        // Produce the map entry
        (run, tuples)
    }
  }.toMap


  private lazy val clicksEntries
  : List[Entry] = runs.flatMap {
    case UserRun(events) =>
      events.flatMap {
        case Clicks(_, _, hash, _, rank, _) =>
          searchMap.contains(hash) match {
            case false => Nil
            case true =>
              val search = searchMap(hash)
              search.resultLog.getEntry(rank).map(List(_)).getOrElse(Nil)
          }
        case _ => Nil
      }
  }

  // Public stats
  def clickCount(engine: Engine)
  : Int = {
    clicksEntries.count {
      case entry => entry.engine match {
        case Some(`engine`) => true
        case _ => false
      }
    }
  }

  def usefulness()
  : Map[Engine, List[Int]] = {
    val all = q4Map.toList.flatMap {
      case (run, votes) =>
        votes.flatMap {
          case (q4, entry, _) => entry.engine match {
            case None => Nil
            case Some(e) => List((e, q4.score.toInt))
          }
        }
    }

    // Group the votes by engine
    all.groupBy(_._1).map {
      case (e, xs) =>
        (e, xs.map(_._2))
    }
  }

}
