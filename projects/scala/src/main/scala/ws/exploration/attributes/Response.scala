package ws.exploration.attributes

import ws.indices.response.NbResults

case class Response(entries: List[Entry],
                    nbResults: NbResults) {
  def getEntry(i: Int)
  : Option[Entry] = i >= 0 && i < entries.size match {
    case true => Some(entries(i))
    case false => None
  }
}
