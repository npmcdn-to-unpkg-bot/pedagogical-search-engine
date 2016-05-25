package ws.exploration.events

import java.sql.Timestamp

import ws.exploration.attributes.CategoryType

case class Messages(autoId: Int,
                    sid: Option[Int],
                    category: CategoryType.Category,
                    content: String,
                    oTimestamp: Option[Timestamp])
extends Event {

}
