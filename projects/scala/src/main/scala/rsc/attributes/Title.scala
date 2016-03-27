package rsc.attributes

import rsc.Types.Spots
import rsc.indexers.Indices

case class Title(label: String,
                 oSpots: Option[Spots] = None,
                 oIndices: Option[Indices] = None)
