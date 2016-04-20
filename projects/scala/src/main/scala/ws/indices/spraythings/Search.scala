package ws.indices.spraythings

case class Search(uris: List[String],
                  mildFrom: Option[Int],
                  mildTo: Option[Int]) {}
