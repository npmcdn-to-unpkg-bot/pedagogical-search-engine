package ws.exploration.events

import java.sql.Timestamp

import ws.exploration.attributes.Response
import ws.indices.SearchLog

case class Searches(autoId: Int,
                    searchHash: Int,
                    sid: Option[Int],
                    searchlog: SearchLog,
                    resultLog: Response,
                    oTimestamp: Option[Timestamp])
extends Event {

}
