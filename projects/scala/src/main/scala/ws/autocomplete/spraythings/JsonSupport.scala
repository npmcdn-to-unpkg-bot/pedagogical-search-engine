package ws.autocomplete.spraythings

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PersonFormat = jsonFormat1(Search)
}
