package ws.exploration.events

import java.sql.Timestamp

case class Clicks(autoId: Int,
                  entryId: String,
                  searchHash: Int,
                  sid: Option[Int],
                  rank: Int,
                  oTimestamp: Option[Timestamp])
extends Event {

}
