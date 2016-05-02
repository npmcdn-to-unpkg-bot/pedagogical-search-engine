package ws.indices.response

case class Entry(entryId: String,
                 title: String,
                 typeText: String,
                 href: String,
                 snippet: String,
                 quality: QualityType.Quality,
                 rank: Int) {}
