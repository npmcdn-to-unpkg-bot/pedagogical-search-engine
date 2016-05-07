package ws.indices.response

import ws.indices.indexentry.EngineType.Engine

case class Entry(entryId: String,
                 title: String,
                 typeText: String,
                 href: String,
                 snippet: String,
                 quality: QualityType.Quality,
                 rank: Int,
                 engine: Engine) {}
