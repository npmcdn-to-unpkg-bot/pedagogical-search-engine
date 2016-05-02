package ws.userstudy.spraythings

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val SearchInputFormat = jsonFormat(SearchInput, "uris")
}
