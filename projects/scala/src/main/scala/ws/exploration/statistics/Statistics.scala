package ws.exploration.statistics

import org.json4s.native.Serialization.read
import rsc.Formatters
import ws.exploration.UserRun
import ws.exploration.attributes._
import ws.exploration.events.{Clicks, Messages, Searches}
import ws.indices.indexentry.EngineType
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
  : Map[UserRun, List[ResolvedQ4]] = {
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

        // Produce the map entry
        (run, deduplicated.toList)
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
          case rq4 => rq4.resultEntry.engine match {
            case None => Nil
            case Some(e) => List((e, rq4.q4.score.toInt))
          }
        }
    }

    // Group the votes by engine
    all.groupBy(_._1).map {
      case (e, xs) =>
        (e, xs.map(_._2))
    }
  }

  /**
    * Usefulness score when there are both wikichimp
    * and bing results
    */
  def usefulnessConcurrency()
  : Map[Engine, List[Int]] = {
    val all = q4Map.toList.flatMap {
      case (run, votes) =>
        votes.flatMap {
          case rq4 @ ResolvedQ4(q4, entry, search, timestamp) =>
            // Check that there are both bing and wc results
            val entries = search.resultLog.entries
            val wc = entries.filter(_.engine.contains(EngineType.Wikichimp))
            val bing = entries.filter(_.engine.contains(EngineType.Bing))
            (wc, bing) match {
              case (xs, ys) if xs.size > 1 && ys.size > 1 =>
                // Check that the vote is on a result with an engine
                entry.engine match {
                  case None => Nil
                  case Some(e) =>
                    // Check that the search has at least one uri
                    search.searchlog.searchTerms.filter(_.uri.nonEmpty) match {
                      case Nil => Nil
                      case _ =>
                        println(rq4)
                        println(entry.entryId + " " + q4.sid)
                        println()
                        List((e, q4.score.toInt))
                    }
                }

              case _ =>
                Nil
            }
        }
    }

    // Group the votes by engine
    all.groupBy(_._1).map {
      case (e, xs) =>
        (e, xs.map(_._2))
    }
  }

  def usefulnessSomeUri()
  : Map[Engine, List[Int]] = {
    val all = q4Map.toList.flatMap {
      case (run, votes) =>
        votes.flatMap {
          case rq4 =>
            // Check that the vote is on a result with an engine
            rq4.resultEntry.engine match {
              case None => Nil
              case Some(e) =>
                // Check that the search has at least one uri
                rq4.search.searchlog.searchTerms.filter(_.uri.nonEmpty) match {
                  case Nil => Nil
                  case xs =>
                    //println(rq4)
                    //println()
                    List((e, rq4.q4.score.toInt))
                }
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
