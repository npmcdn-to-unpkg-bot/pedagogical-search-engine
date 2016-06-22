package ws.exploration

import java.sql.Timestamp

import org.json4s.native.Serialization._
import rsc.Formatters
import ws.exploration.attributes.Q3Type.Q3Type
import ws.exploration.attributes.{Q3Type, _}
import ws.exploration.events.{Clicks, Event, Messages, Searches}
import ws.indices.indexentry.EngineType
import ws.indices.spraythings.SearchTerm

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

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

  lazy val clicks
  : List[Clicks] =
    ordered.flatMap {
      case click if click.isInstanceOf[Clicks] => List(click.asInstanceOf[Clicks])
      case _ => Nil
    }

  lazy val resolvedClicks
  : List[ResolvedClick] = {
    clicks.map(click => {
      ResolvedClick.fromRun(this, click)
    })
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

  lazy val q1Vote
  : Option[Boolean] = {
    // Collect the q2 answers
    val q1Answers = ordered.flatMap {
      case msg @ Messages(_, _, CategoryType.Feedback, content, _) =>
        try {
          read[Feedback](content) match {
            case Feedback(QuestionIdType.Q1, valueStr) =>
              val value = valueStr match {
                case "yes" => true
                case "no" => false
                case "joker" => throw new Exception("")
              }
              List((msg.timestamp(), value))
            case _ => Nil
          }
        } catch {
          case e: Throwable => Nil
        }

      case _ => Nil
    }

    // Extract the vote of the latest one
    q1Answers.sortBy(-_._1.getTime) match {
      case Nil => None
      case head::tail => Some(head._2)
    }
  }

  lazy val q2Vote
  : Option[Boolean] = {
    // Collect the q2 answers
    val q2Answers = ordered.flatMap {
      case msg @ Messages(_, _, CategoryType.Feedback, content, _) =>
        try {
          read[Feedback](content) match {
            case Feedback(QuestionIdType.Q2, valueStr) =>
              val value = valueStr match {
                case "yes" => true
                case "no" => false
                case "joker" => throw new Exception("")
              }
              List((msg.timestamp(), value))
            case _ => Nil
          }
        } catch {
          case e: Throwable => Nil
        }

      case _ => Nil
    }

    // Extract the vote of the latest one
    q2Answers.sortBy(-_._1.getTime) match {
      case Nil => None
      case head::tail => Some(head._2)
    }
  }

  lazy val q3Vote
  : Option[Q3Type] = {
    // Collect the q3 answers
    val q3Answers = ordered.flatMap {
      case msg @ Messages(_, _, CategoryType.Feedback, content, _) =>
        try {
          read[Feedback](content) match {
            case Feedback(QuestionIdType.Q3, valueStr) =>
              val value = Q3Type.withName(valueStr)
              List((msg.timestamp(), value))
            case _ => Nil
          }
        } catch {
          case e: Throwable => Nil
        }

      case _ => Nil
    }

    // Extract the vote of the latest one
    q3Answers.sortBy(-_._1.getTime) match {
      case Nil => None
      case head::tail => Some(head._2)
    }
  }

  lazy val entries: List[Entry] = {
    responses.flatMap {
      case response => response.entries
    }
  }

  lazy val entriesWithoutWCFT: List[Entry] = {
    responsesWithoutWCFT.flatMap {
      case response => response.entries
    }
  }

  lazy val entriesWithoutWCFTInRun: List[Entry] = {
    responsesWithoutWCFTInRun.flatMap {
      case response => response.entries
    }
  }

  lazy val responses: List[Response] = {
    searches.map {
      case search => search.resultLog
    }
  }

  lazy val responsesWithoutWCFT: List[Response] = {
    searches.flatMap(search => {
      val exists = search.resultLog.entries.exists {
        case wcft if wcft.engine.contains(EngineType.WikichimpFT) => true
        case _ => false
      }
      exists match {
        case true => Nil
        case false => List(search.resultLog)
      }
    })
  }

  lazy val responsesWithoutWCFTInRun: List[Response] = {
    searches.flatMap(search => {
      val exists = search.resultLog.entries.exists {
        case wcft if wcft.engine.contains(EngineType.WikichimpFT) => true
        case _ => false
      }
      exists match {
        case true => List(true)
        case false => Nil
      }
    })
    searches.isEmpty match {
      case true => responses
      case false => Nil
    }
  }

  lazy val responseWithAnyWikichimp
  : List[Response] = {
    responses.flatMap(response => {
      val existsWc = response.entries.flatMap {
        case wc if wc.engine.contains(EngineType.Wikichimp) => Some(true)
        case _ => None
      }
      existsWc match {
        case Nil => Nil
        case _ => List(response)
      }
    })
  }

  lazy val searches:
    List[Searches] = {
    ordered.flatMap {
      case search if search.isInstanceOf[Searches] =>
        List(search.asInstanceOf[Searches])
      case _ => Nil
    }
  }

  lazy val resolvedQ4WcHit
  : List[ResolvedQ4] = {
    resolvedQ4.flatMap(rq4 => {
      val response = rq4.search.resultLog
      val existWc = response.entries.exists {
        case entry if entry.engine.contains(EngineType.Wikichimp) => true
        case _ => false
      }
      existWc match {
        case true => List(rq4)
        case false => Nil
      }
    })
  }

  lazy val q4RatingsWithScores: List[(Int, Double)] = {
    val futures = resolvedQ4.map(rq4 => {
      rq4.resultEntry.engine match {
        case Some(EngineType.Wikichimp) =>
          val rating = rq4.q4.score.toInt
          val resultEntry = rq4.resultEntry
          val entryId = resultEntry.entryId
          val uris = rq4.search.searchlog.searchTerms.flatMap(st => st.uri)

          MoreData.entryScore(uris.toSet, entryId).map {
            case None => Nil
            case Some(score) => List((rating, score))
          }


        case _ =>
          Future.successful(Nil)
      }
    })

    val merged = Future.sequence(futures)
    Await.result(merged, Duration.Inf).flatten
  }

  lazy val q4RatingsWithRanks: List[(Int, Int)] = {
    resolvedQ4.flatMap(rq4 => {
      rq4.resultEntry.engine match {
        case Some(EngineType.Wikichimp) =>
          val rank = rq4.resultEntry.rank
          val rating = rq4.q4.score.toInt
          List((rating, rank))

        case _ => Nil
      }
    })
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

  def eventsBefore[U <: Event](events: List[U], t: Timestamp)
  : List[U] = {
    val acc: List[U] = Nil
    events.foldLeft(acc) {
      case (a, event) => event.timestamp().before(t) match {
        case true => a ::: List(event)
        case false => a
      }
    }
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
