package ws.exploration.attributes

import ws.exploration.UserRun
import ws.exploration.events.{Clicks, Searches}

case class ResolvedClick(click: Clicks,
                         entry: Entry,
                         search: Searches) {
}

object ResolvedClick {
  def fromRun(run: UserRun, click: Clicks)
  : ResolvedClick = {
    val searches = UserRun.eventsBefore(run.searches, click.timestamp())
    val candidates = searches.flatMap(search => {
      val entriesIds = search.resultLog.entries.map(_.entryId)
      entriesIds.indexOf(click.entryId) match {
        case -1 => Nil
        case i => List((search, search.resultLog.entries(i)))
      }
    })
    val (search, entry) = candidates.last
    ResolvedClick(click, entry, search)
  }
}
