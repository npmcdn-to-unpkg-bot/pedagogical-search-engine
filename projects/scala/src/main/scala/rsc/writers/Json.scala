package rsc.writers

import rsc.{Formatters, Resource}
import utils.Files

object Json extends Writer with Formatters {
  def executeWrite(resource: Resource) = {
    val json = org.json4s.native.Serialization.writePretty(resource)
    val path1 = settings.Resources.folder
    val path2 = resource.friendlyPath
    Files.write(json, s"$path1/$path2.json")
  }
}
