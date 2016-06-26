package agents

import java.io.File

import agents.helpers.FileExplorer
import org.json4s.native.JsonMethods._
import rsc.indexers.Graph2
import rsc.writers.Json
import rsc.{Formatters, Resource}
import utils.Settings

import scala.concurrent.{ExecutionContext, Future}

object IndexWithGraphs extends App with Formatters {

  // Import the settings
  val settings = new Settings()

  // Create the file explorer
  val explorer = new FileExplorer("index-with-graphs",
    nbTasks = settings.Indices.Indexation.nbTasks,
    forceProcess = true)

  // Define how the resources are processed
  def process(file: File, ec: ExecutionContext): Future[Option[String]] = {
    // Parse
    val json = parse(file)
    val r = json.extract[Resource]

    // Index
    val indexer = new Graph2(ec, fizzFactor = 1)
    indexer.index(r).flatMap {
      case Some(newR) =>
        Json.write(newR, Some(file.getAbsolutePath))
        Future.successful(Some("Ok"))

      case None =>
        // The resource was rejected, we do nothing
        Future.successful(Some("The resource seems hard to index. The algorithm rejected it."))
    }(ec)
  }

  explorer.launch(process)
}
