package ws.indices.spraythings

case class Search(uris: List[String],
                  from: Option[Int],
                  to: Option[Int]) {}
