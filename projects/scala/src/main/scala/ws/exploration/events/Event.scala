package ws.exploration.events

import java.sql.Timestamp

trait Event {
  def timestamp()
  : Timestamp = oTimestamp.getOrElse(new Timestamp(0))

  def oTimestamp: Option[Timestamp]
}
