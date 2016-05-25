package ws.exploration.statistics

import ws.exploration.UserRun
import ws.exploration.attributes.Entry
import ws.exploration.events.{Clicks, Messages, Searches}
import ws.indices.indexentry.EngineType.Engine

class Statistics(runs: List[UserRun],
                 clicks: List[Clicks],
                 messages: List[Messages],
                 searches: List[Searches]) {

  private lazy val searchMap
  : Map[Int, Searches] = searches.map {
    case search => (search.searchHash, search)
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

  def clickCount(engine: Engine)
  : Int = {
    clicksEntries.count {
      case entry => entry.engine match {
        case Some(`engine`) => true
        case _ => false
      }
    }
  }

}
