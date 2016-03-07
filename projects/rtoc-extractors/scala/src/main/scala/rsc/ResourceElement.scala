package rsc

import org.json4s._
import org.json4s.native.JsonMethods._
import rsc.Types.{Descriptions, Metadata, TOCs}

case class ResourceElement(oMetadata: Option[Metadata],
                           oTocs: Option[TOCs],
                           oDescriptions: Option[Descriptions]) {
  def mergeWith(rs: ResourceElement*): ResourceElement = {
    // Apply merges one after the others
    rs.foldLeft(this)((e1, e2) => {
      // Merge fields
      val meta = (e1.oMetadata, e2.oMetadata) match {
        case (None, x) => x
        case (x, None) => x
        case (Some(m1), Some(m2)) => Some(m1 merge m2)
      }
      val toc = mergeLists(e1.oTocs, e2.oTocs)
      val des = mergeLists(e1.oDescriptions, e2.oDescriptions)

      // Create new element
      ResourceElement(meta, toc, des)
    })
  }

  private def mergeLists[U](l1: Option[List[U]], l2: Option[List[U]]): Option[List[U]] = (l1, l2) match {
    case (None, x) => x
    case (x, None) => x
    case (Some(t1), Some(t2)) => Some(t1:::t2)
  }

  def resource(folder: String, name: String): Resource =
    new Resource(oMetadata, oTocs, oDescriptions, folder, name)
}
