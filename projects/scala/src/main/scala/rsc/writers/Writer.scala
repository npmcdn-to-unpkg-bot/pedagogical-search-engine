package rsc.writers

import rsc.Resource
import utils.{Logger, Settings}

abstract class Writer(_settings: Settings) {

  val settings = _settings

  def this() = this(new Settings())

  def write(resource: Resource) = {
    val source = resource.source
    val title = resource.title

    executeWrite(resource)
    Logger.info(s"Written: $source, $title")
  }

  protected def executeWrite(resource: Resource)
}
