package ws.indices.spraythings

case class Search(searchTerms: List[SearchTerm],
                  from: Option[Int],
                  to: Option[Int]) {}
