package rsc.writers

import rsc.{Formatters, Resource}
import utils.Files

object Json extends Writer with Formatters {
  override protected def executeWrite(resource: Resource, oAbsolutePath: Option[String]) = {
    val json = org.json4s.native.Serialization.writePretty(resource)
    val path = oAbsolutePath match {
      case None => {
        val path2 = resource.friendlyPath
        val path1 = settings.Resources.folder
        s"$path1/$path2.json"
      }
      case Some(x) => x
    }
    Files.write(json, path)
  }
}
