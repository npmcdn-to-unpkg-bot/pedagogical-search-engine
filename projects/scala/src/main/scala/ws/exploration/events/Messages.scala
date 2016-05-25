package ws.exploration.events

import java.sql.Timestamp

case class Messages(autoId: Int,
                    sid: Option[Int],
                    category: String,
                    content: String,
                    oTimestamp: Option[Timestamp])
extends Event {

}
