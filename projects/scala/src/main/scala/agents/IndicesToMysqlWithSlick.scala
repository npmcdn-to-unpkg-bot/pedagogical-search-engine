package agents

import java.io.File

import agents.helpers.FileExplorer
import org.json4s.native.JsonMethods._
import rsc.importers.SlickMysql
import rsc.indexers.Indexer
import rsc.{Formatters, Resource}
import slick.jdbc.JdbcBackend._
import utils.Settings

import scala.concurrent.{ExecutionContext, Future}

object IndicesToMysqlWithSlick extends App with Formatters {

  // Create the connection to the database
  val db = Database.forConfig("wikichimp.indices.import.slick")

  // Import the settings
  val settings = new Settings()

  // The indices are imported through an importer
  lazy val importer = new SlickMysql(db, settings.Indices.Import.topIndicesNumber)

  // Create the file explorer
  val explorer = new FileExplorer("indices-to-mysql-with-slick", forceProcess = false)

  def process(file: File, ec: ExecutionContext): Future[Option[String]] = {
    // Parse the resource
    val json = parse(file)
    val r = json.extract[Resource]

    r.oIndexer match {
      case Some(Indexer.GraphChoiceBased) =>
        importer.importResource(r, ec).flatMap {
          case _ => Future.successful(None)
        }(ec)

      case _ =>
        Future.successful(Some("The resource is not indexed yet."))
    }
  }

  explorer.launch(process)
}
