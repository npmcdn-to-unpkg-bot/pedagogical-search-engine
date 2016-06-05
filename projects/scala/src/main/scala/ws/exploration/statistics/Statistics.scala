package ws.exploration.statistics

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
  : Map[UserRun, List[ResolvedQ4]] =
    runs.map(run => (run, run.resolvedQ4)).toMap

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
  def searchesCount()
  : Map[Int, Int] = {
    val counts = runs.flatMap(run => {
      run.searches.map(search => {
        search.searchlog.searchTerms.size
      })
    })

    counts.groupBy(x => x).map {
      case (x, group) => (x, group.size)
    }
  }

  def allSearchedUrisDistinctByRun()
  : List[String] = {
    runs.flatMap(run => {
      // For each run, take the distinct list of uris
      run.searches.flatMap(search => {
        search.searchlog.searchTerms.flatMap(_.uri)
      }).distinct
    })
  }

  def totalQ3Votes()
  : Int = runs.count(run => run.q3Vote.isDefined)

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

  def usefulnessWcHit()
  : Map[Engine, List[Int]] = {
    val allVotes = runs.flatMap(run => {
      // For each run, get the votes
      run.resolvedQ4WcHit.flatMap(rq4 => {
        rq4.resultEntry.engine match {
          case Some(engine) =>
            Some((engine, rq4.q4.score.toInt))
          case _ => None
        }
      })
    })

    allVotes.groupBy(_._1).map{
      case (engine, xs) => (engine, xs.map(_._2))
    }
  }

  def usefulnessClicked()
  : Map[Engine, List[Int]] = {
    val votes = runs.flatMap(run => {
      val rClicks = run.resolvedClicks
      val clickedId = rClicks.map(_.entry.entryId)

      // Filter the ratings on entries which have been clicked
      val clickedQ4 = run.resolvedQ4.flatMap(rq4 => {
        val entry = rq4.resultEntry
        clickedId.contains(entry.entryId) match {
          case true => List(rq4)
          case false => Nil
        }
      })

      // Collect the votes
      clickedQ4.flatMap(q4 => {
        // Ensure that the vote is
        q4.resultEntry.engine.map(engine => {
          (engine, q4.q4.score.toInt)
        })
      })
    })

    // Group the votes by engine
    votes.groupBy(_._1).map{
      case (engine, xs) => (engine, xs.map(_._2))
    }
  }

  def satisfactionQ3(entriesFn: UserRun => List[Entry])
  : Map[Q3Type.Q3Type, Map[EngineType.Engine, Int]] = {
    // Get the votes
    val maps = runs.flatMap(run => {
      run.q3Vote match {
        case None => Nil
        case Some(vote) =>
          // Count the results(which lead to this vote) by type
          val engines = entriesFn(run).flatMap {
            case entry => entry.engine
          }

          val counts = engines.groupBy(e => e).map {
            case (e, xs) => (e, xs.size)
          }

          // Produce the count
          List((vote, counts))
      }
    })

    // Merge the votes by value
    maps.groupBy(_._1).map {
      case (voteValue, stats) =>
        val engineCounts = stats.flatMap(_._2.toList).groupBy(_._1).map {
          case (engine, group2) =>
            (engine, group2.map(_._2).sum)
        }
        (voteValue, engineCounts)
    }
  }

  def satisfactionQ3All()
  : Map[Q3Type.Q3Type, Map[EngineType.Engine, Int]] = {
    def allEntries(run: UserRun): List[Entry] = run.entries
    satisfactionQ3(allEntries)
  }

  def satisfactionQ3WithoutWCFT()
  : Map[Q3Type.Q3Type, Map[EngineType.Engine, Int]] = {
    def entriesFn(run: UserRun): List[Entry] = run.entriesWithoutWCFT
    satisfactionQ3(entriesFn)
  }

  def satisfactionQ3WithoutWCFTInRun()
  : Map[Q3Type.Q3Type, Map[EngineType.Engine, Int]] = {
    def entriesFn(run: UserRun): List[Entry] = run.entriesWithoutWCFTInRun
    satisfactionQ3(entriesFn)
  }

  def satisfactionQ3Click()
  : Map[Q3Type.Q3Type, Map[EngineType.Engine, Int]] = {
    // Get the votes
    val maps = runs.flatMap(run => {
      run.q3Vote match {
        case None => Nil
        case Some(vote) =>
          // Count the results(which lead to this vote) by type
          val engines = run.resolvedClicks.flatMap(rclick => {
            rclick.entry.engine match {
              case None => Nil
              case Some(e) => List(e)
            }
          })

          val counts = engines.groupBy(e => e).map {
            case (e, xs) => (e, xs.size)
          }

          // Produce the count
          List((vote, counts))
      }
    })

    // Merge the votes by value
    maps.groupBy(_._1).map {
      case (voteValue, stats) =>
        val engineCounts = stats.flatMap(_._2.toList).groupBy(_._1).map {
          case (engine, group2) =>
            (engine, group2.map(_._2).sum)
        }
        (voteValue, engineCounts)
    }
  }

  def satisfiedPeopleEntries()
  : Map[EngineType.Engine, Int] = {
    // Collect the Q4 votes
    val votes = runs.flatMap(run => {
      // .. but only from satisfied people
      run.q3Vote match {
        case Some(Q3Type.Potential) | Some(Q3Type.Better) =>
          q4Map(run).flatMap(rq4 => {
            rq4.resultEntry.engine match {
              case None => Nil
              case Some(engine) =>
                // Is the result useful?
                val score = rq4.q4.score.toInt
                if(score > 2) {
                  List((engine, 1))
                } else {
                  Nil
                }
            }
          })

        case _ => Nil
      }
    })

    // Group the votes by engine
    votes.groupBy(_._1).map {
      case (engine, xs) => (engine, xs.map(_._2).sum)
    }
  }

  /**
    * The proportion of people using the suggestion functionality.
    * [researches that contains at least one suggestion term]
    */
  def suggestionProportion()
  : Double = {
    val partial = runs.map(run => {
      val searchTermsBySearch = run.ordered.filter(_.isInstanceOf[Searches])
        .map(_.asInstanceOf[Searches].searchlog.searchTerms)
        .distinct


      val withSuggestions = searchTermsBySearch.filter(searchTerms => {
        val existFtTerm = searchTerms.exists {
          case SearchTerm(_, None) => true
          case _ => false
        }
        !existFtTerm
      })

      val n = searchTermsBySearch.size
      val s = withSuggestions.size

      (n, s)
    })

    val tot = partial.map(_._1).sum
    val withSugg = partial.map(_._2).sum

    withSugg.toDouble / tot.toDouble
  }

  /**
    * The proportion of searches that give at least one result from the Wikichimp engine.
    */
  def wcHitProportion()
  : Double = {
    // For each run, count the number of response with and without any wc result
    val counts = runs.map(run => {
      val withWc = run.responseWithAnyWikichimp.size
      val all = run.responses.size
      (all, withWc)
    })

    val tot = counts.map(_._1).sum
    val withWc = counts.map(_._2).sum

    withWc.toDouble / tot.toDouble
  }

  /**
    * best usefulness score given on each search for Wikichimp
    * and Bing when the user scored at least one of each.
    * Soft means that a search correponds to a results page.
    */
  def usefulnessSoftBestComparison()
  : Map[Engine, List[Int]] = {
    val all = q4Map.toList.flatMap {
      case (run, votes) =>
        // Group by search
        votes.groupBy(_.search.searchlog).toList.flatMap {
          case (_, rq4s) =>
            // Does there exist at least one vote on each sort of result?
            val wcs = rq4s.filter(_.resultEntry.engine.contains(EngineType.Wikichimp))
            val bings = rq4s.filter(_.resultEntry.engine.contains(EngineType.Bing))
            wcs.nonEmpty && bings.nonEmpty match {
              case false => Nil
              case true =>
                // Take the best ones
                val topWc = wcs.sortBy(-_.q4.score.toInt).head
                val topBing = bings.sortBy(-_.q4.score.toInt).head
                (topWc.resultEntry.engine, topBing.resultEntry.engine) match {
                  case (Some(EngineType.Wikichimp), Some(EngineType.Bing)) =>
                    val wcTuple = (EngineType.Wikichimp, topWc.q4.score.toInt)
                    val bingTuple = (EngineType.Bing, topBing.q4.score.toInt)
                    List(wcTuple, bingTuple)
                  case _ => Nil
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

  /**
    * best usefulness score given on each search for Wikichimp
    * and Bing when the user scored at least one of each.
    * Hard means that a research may correspond to multiple results
    * pages, the important is that it is about the same search terms.
    */
  def usefulnessBestHardComparison()
  : Map[Engine, List[Int]] = {
    val all = q4Map.toList.flatMap {
      case (run, votes) =>
        // Group by search
        votes.groupBy(_.search.searchlog.searchTerms).toList.flatMap {
          case (_, rq4s) =>
            // Does there exist at least one vote on each sort of result?
            val wcs = rq4s.filter(_.resultEntry.engine.contains(EngineType.Wikichimp))
            val bings = rq4s.filter(_.resultEntry.engine.contains(EngineType.Bing))
            wcs.nonEmpty && bings.nonEmpty match {
              case false => Nil
              case true =>
                // Take the best ones
                val topWc = wcs.sortBy(-_.q4.score.toInt).head
                val topBing = bings.sortBy(-_.q4.score.toInt).head
                (topWc.resultEntry.engine, topBing.resultEntry.engine) match {
                  case (Some(EngineType.Wikichimp), Some(EngineType.Bing)) =>
                    val wcTuple = (EngineType.Wikichimp, topWc.q4.score.toInt)
                    val bingTuple = (EngineType.Bing, topBing.q4.score.toInt)
                    List(wcTuple, bingTuple)
                  case _ => Nil
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

  /**
    * Usefulness score when the user score at least one
    * Wikichimp and one Bing result.
    */
  def usefulnessComparison()
  : Map[Engine, List[Int]] = {
    val all = q4Map.toList.flatMap {
      case (run, votes) =>
        // Group by search
        votes.groupBy(_.search.searchlog).toList.flatMap {
          case (_, rq4s) =>
            // Does there exist at least one vote on each sort of result?
            val wcs = rq4s.filter(_.resultEntry.engine.contains(EngineType.Wikichimp))
            val bings = rq4s.filter(_.resultEntry.engine.contains(EngineType.Bing))
            wcs.nonEmpty && bings.nonEmpty match {
              case false => Nil
              case true => (wcs ::: bings).flatMap {
                case rq4 => rq4.resultEntry.engine match {
                  case None => Nil
                  case Some(e) => List((e, rq4.q4.score.toInt))
                }
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
                      case _ => List((e, q4.score.toInt))
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

  def simplySatisfactionQ3():
  Map[Q3Type.Q3Type, Int] = {
    val votes = runs.flatMap(run => {
      run.q3Vote match {
        case None => Nil
        case Some(vote) =>
          List(vote)
      }
    })
    votes.groupBy(x => x).map {
      case (vote, xs) => (vote, xs.size)
    }
  }

}
