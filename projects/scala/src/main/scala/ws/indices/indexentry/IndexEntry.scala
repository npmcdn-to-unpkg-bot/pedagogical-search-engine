package ws.indices.indexentry

import rsc.attributes.Source.Source

trait IndexEntry {
  def engine: EngineType.Engine
  def source: Source
}
