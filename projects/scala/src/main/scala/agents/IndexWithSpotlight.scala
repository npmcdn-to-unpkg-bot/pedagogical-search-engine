package agents

import java.io.File

import agents.helpers.FileExplorer
import org.json4s.native.JsonMethods._
import rsc.writers.Json
import rsc.{Formatters, Resource}

import scala.concurrent.{ExecutionContext, Future}

object IndexWithSpotlight extends App with Formatters {
  // Create the file explorer
  val explorer = new FileExplorer("indices-spotlight", forceProcess = true)

  def process(file: File, ec: ExecutionContext): Future[Any] = {
    // Parse it
    val json = parse(file)
    val r = json.extract[Resource]


    val name = file.getAbsolutePath
    new rsc.indexers.Spotlight().index(r) match {
      case None =>
        Future.failed(new Exception(s"Cannot index: $name"))

      case Some(newR) =>
        Json.write(newR, Some(file.getAbsolutePath))
        Future.successful()
    }
  }

  explorer.launch(process)
}
