package rsc.writers

import rsc.Resource
import utils.Settings

abstract class Writer(_settings: Settings) {

  val settings = _settings

  def this() = this(new Settings())

  def write(resource: Resource, oAbsolutePath: Option[String] = None) = {
    val source = resource.source
    val title = resource.title

    executeWrite(resource, oAbsolutePath)
  }

  protected def executeWrite(resource: Resource, oAbsolutePath: Option[String])
}
