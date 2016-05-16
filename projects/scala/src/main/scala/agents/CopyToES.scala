package agents

import java.io.File

import agents.helpers.FileExplorer
import akka.actor.ActorSystem
import elasticsearch.ResourceWriter
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write
import rsc.{Formatters, Resource}
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse, StatusCodes}
import utils.Settings

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object CopyToES extends App with Formatters {
  // Create the file explorer
  val explorer = new FileExplorer("snippetizer", forceProcess = true)

  // Create the http actor
  val settings = new Settings()
  implicit private val system = ActorSystem("es-import")
  import system.dispatcher
  val timeoutMs = 10*60*1000
  val timeout = timeoutMs.milliseconds

  val ip = settings.ElasticSearch.ip
  val port = settings.ElasticSearch.httpApiPort
  val esIndex = settings.ElasticSearch.esIndex
  val esType = settings.ElasticSearch.esType
  val url = s"http://$ip:$port/_bulk"

  val pipeline: HttpRequest => Future[HttpResponse] = (
    addHeader("Accept", "application/json")
      ~> sendReceive(implicitly, implicitly, futureTimeout = timeout)
    )

  // Define how each file is processed
  val esComplement: JObject =
    "index" -> (
      ("_index" -> esIndex) ~
        ("_type" -> esType)
      )
  val esComplementStr: String = write(esComplement)
  def process(file: File, ec: ExecutionContext): Future[Any] = {
    // Parse it
    val json = parse(file)
    val r = json.extract[Resource]

    // Create the es objects
    val objects = ResourceWriter.jsonResources(r)
    val body = objects.map(o => esComplementStr + "\n" + write(o)).mkString("\n")

    // Execute the bulk import and detect failures
    pipeline(Post(url, body + "\n")).map {
      case response =>
        if(response.status != StatusCodes.OK) {
          throw new Exception(s"Failed to import ${file.getAbsolutePath}, " +
            s"es api responded with http code: ${response.status}}")
        }
    }
  }

  explorer.launch(process)

  // Shut down the http actor
  system.shutdown()
}
