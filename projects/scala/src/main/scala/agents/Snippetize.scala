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
  val explorer = new FileExplorer("snippetizer", forceProcess = false)

  // Create the snippetizer
  val snippetizer = new Simple()

  // Define how each file is processed
  def process(file: File, ec: ExecutionContext): Future[Option[String]] = {
    // Open the resource
    val json = parse(file)
    val r = json.extract[Resource]
    val absolute = file.getAbsolutePath

    r.oIndexer match {
      case Some(Indexer.GraphChoiceBased) =>
        //  Snippetize
        val newR = snippetizer.snippetize(r)

        // Write the result
        Json.write(newR, Some(absolute))
        Future.successful(None)

      case _ =>
        Future.failed(new Exception("The resource is not indexed yet."))
    }
  }

  explorer.launch(process)
}
