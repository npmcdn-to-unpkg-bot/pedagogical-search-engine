package ws.userstudy.spraythings

import ws.indices.spraythings.SearchTerm

case class ClassificationInput(searchTerms: List[SearchTerm],
                               entryId: String,
                               classification: String,
                               sid: Option[Int]) {}
