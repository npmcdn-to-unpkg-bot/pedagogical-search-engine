package ws.exploration.attributes

import java.sql.Timestamp

import ws.exploration.events.Searches

case class ResolvedQ4(q4: Q4Entry,
                      resultEntry: Entry,
                      search: Searches,
                      timestamp: Timestamp) {
  override def toString: String = {
    // Search terms
    val searchTerms = search.searchlog.searchTerms.mkString("\n")

    // Results list
    val results = search.resultLog.entries.map {
      case entry =>
        val engine = entry.engine
        val title = entry.title
        val pointer = entry.entryId == resultEntry.entryId match {
          case true => s"[scored: ${q4.score}] "
          case false => ""
        }
        s"$pointer$title - $engine"
    }.mkString("\n")
    s"Search terms: $searchTerms\nResults:\n$results"
  }
}
