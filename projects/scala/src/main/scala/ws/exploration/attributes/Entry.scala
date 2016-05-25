package ws.exploration.attributes

import ws.indices.indexentry.EngineType._
import ws.indices.response.QualityType

case class Entry(entryId: String,
                 title: String,
                 source: String,
                 href: String,
                 snippet: String,
                 quality: QualityType.Quality,
                 rank: Int,
                 oEngine: Option[Engine],
                 topUris: Option[List[String]]) {}
