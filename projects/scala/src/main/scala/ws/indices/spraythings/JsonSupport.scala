package ws.indices.spraythings

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val SearchFormat = jsonFormat(Search, "uris", "from", "to")
}
