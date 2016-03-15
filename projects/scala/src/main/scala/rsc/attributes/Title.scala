package rsc.attributes

import rsc.Types.{Indices, Spots}

case class Title(label: String,
                 oSpots: Option[Spots] = None,
                 oIndices: Option[Indices] = None)
