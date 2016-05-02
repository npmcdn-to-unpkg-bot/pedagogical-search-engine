package ws.indices.response

case class Entry(title: String,
                 typeText: String,
                 href: String,
                 snippet: String,
                 quality: QualityType.Quality,
                 rank: Int) {}
