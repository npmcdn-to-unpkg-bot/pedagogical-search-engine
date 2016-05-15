package agents

import java.io.File

import agents.helpers.FileExplorer
import org.json4s.native.JsonMethods._
import rsc.indexers.Indexer
import rsc.snippets.Simple
import rsc.writers.Json
import rsc.{Formatters, Resource}

import scala.concurrent.{ExecutionContext, Future}

object Snippetize extends App with Formatters {
  // Create the file explorer
  val explorer = new FileExplorer("snippetizer", forceProcess = true)

  // Create the snippetizer
  val snippetizer = new Simple()

  // Define how each file is processed
  def process(file: File, ec: ExecutionContext): Future[Unit] = {
    // Open the resource
    val json = parse(file)
    val r = json.extract[Resource]
    val absolute = file.getAbsolutePath

    // Was it already indexed?
    val indexed = r.oIndexer match {
      case None => false
      case Some(i) => i match {
        case Indexer.Graph => true
        case _ => false
      }
    }

    indexed match {
      case false =>
        Future.failed(new Exception(s"Resource not indexed: $absolute"))

      case true =>
        //  Snippetize
        val newR = snippetizer.snippetize(r)

        // Write the result
        Json.write(newR, Some(absolute))

        Future.successful()
    }

  }

  explorer.launch(process)
}
