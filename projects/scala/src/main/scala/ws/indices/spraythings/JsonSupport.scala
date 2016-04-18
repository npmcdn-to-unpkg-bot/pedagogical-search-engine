package ws.indices.spraythings

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val SearchFormat = jsonFormat1(Search)
}
