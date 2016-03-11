package utils

import com.typesafe.config.{Config, ConfigFactory}

class Settings(config: Config) {

  val namespace = "wikichimp"

  // default configuration
  def this() {
    this(ConfigFactory.load())
  }

  private def getString(setting: String): String = config.getString(s"$namespace.$setting")

  private def getInt(setting: String): Int = config.getInt(s"$namespace.$setting")

  // fields
  object Spotlight {
    val port = getInt("spotlight.server.port")
    val host = getString("spotlight.server.host")
  }
}