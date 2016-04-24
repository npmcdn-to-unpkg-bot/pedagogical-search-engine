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
  val workingDir = getString("workingDir")
  object Spotlight {
    val port = getInt("spotlight.server.port")
    val host = getString("spotlight.server.host")
  }
  object Wikipedia {
    object Links {
      object Mysql {
        val ip = getString("wikipedia.links.mysql.ip")
        val port = getInt("wikipedia.links.mysql.port")
        val database = getString("wikipedia.links.mysql.database")
        val user = getString("wikipedia.links.mysql.user")
        val password = getString("wikipedia.links.mysql.password")
      }
    }
  }
  object Autocomplete {
    object Spray {
      val ip = getString("autocomplete.spray.ip")
      val port = getInt("autocomplete.spray.port")
    }
  }
  object Indices {
    object Indexation {
      val nbTasks = getInt("indices.indexation.nbTasks")
    }
    object Ws {
      object Spray {
        val ip = getString("indices.ws.spray.ip")
        val port = getInt("indices.ws.spray.port")
      }
    }
  }
  object Resources {
    val folder = getString("resources.folder")
    object Coursera {
      val pages = getString("resources.coursera.pages")
      val data = getString("resources.coursera.data")
    }
    object Khan {
      val pages = getString("resources.khan.pages")
    }
    object Mit {
      val pages = getString("resources.mit.pages")
      val data = getString("resources.mit.data")
    }
    object Safari {
      val pages = getString("resources.safari.pages")
    }
    object Scholarpedia {
      val pages = getString("resources.scholarpedia.pages")
      val data = getString("resources.scholarpedia.data")
    }
  }
}