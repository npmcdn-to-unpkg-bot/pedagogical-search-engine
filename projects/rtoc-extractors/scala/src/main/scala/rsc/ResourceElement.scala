package rsc

import rsc.Types.{Descriptions, Metadata, TOCs}

case class ResourceElement(oMetadata: Option[Metadata],
                           oTocs: Option[TOCs],
                           oDescriptions: Option[Descriptions]) {}
