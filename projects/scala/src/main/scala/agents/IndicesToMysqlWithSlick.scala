package agents

import java.io.File

import agents.helpers.FileExplorer
import org.json4s.native.JsonMethods._
import rsc.importers.SlickMysql
import rsc.indexers.Indexer
import rsc.{Formatters, Resource}
import slick.jdbc.JdbcBackend._

import scala.concurrent.{ExecutionContext, Future}

object IndicesToMysqlWithSlick extends App with Formatters {

  // Create the connection to the database
  val db = Database.forConfig("wikichimp.indices.import.slick")

  // The indices are imported through an importer
  lazy val importer = new SlickMysql(db)

  // Create the file explorer
  val explorer = new FileExplorer("indices-to-mysql-with-slick", forceProcess = true)

  def process(file: File, ec: ExecutionContext): Future[Any] = {
    // Parse the resource
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

    // Import the indices only if the resource is indexed
    indexed match {
      case false =>
        Future.failed(new Exception(s"Resource is not yet indexed: $absolute"))

      case true =>
        importer.importResource(r, ec)
    }
  }

  explorer.launch(process)
}
