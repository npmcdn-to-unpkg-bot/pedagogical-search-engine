package ws.userstudy.spraythings

import ws.indices.spraythings.SearchTerm

case class ClickInput(searchTerms: List[SearchTerm],
                      entryId: String,
                      rank: Int,
                      sid: Option[Int]) {
}
