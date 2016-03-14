package agents

import java.io.File

import rsc.Types.Spots
import rsc.attributes.Title
import rsc.printers.Spots
import rsc.writers.Json
import rsc.{Resource, Formatters}
import rsc.printers.Spots
import spotlight.WebService
import utils.{Files, Settings}
import org.json4s.native.JsonMethods._

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()
    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)

    case class Actor(text: String, fn: (Resource, Spots) => Resource)
    case class Work(spots: Spots, actor: Actor)

    def annotate(resource: Resource, actors: List[Actor]): Option[Resource] = {
      // Annotate each text
      val texts = actors.map(_.text)
      val oSpots = webService.textsToSpots(texts)

      // Let the actors work on the results
      oSpots match {
        case None => None
        case Some(ls) => {
          val work = ls.zip(actors).map(p => Work(p._1, p._2))
          val newResource = work.foldLeft(resource)((rsc, w) => w.spots match {
            case Nil => rsc // No spots = No work
            case _ => w.actor.fn(rsc, w.spots)
          })

          Some(newResource)
        }
      }
    }

    // Create the actors
    def newTitle(resource: Resource, spots: Spots): Resource =
      resource.copy(title = resource.title.copy(oSpots = Some(spots)))

    // Annotate each resource-file
    Files.explore(new File(settings.Resources.folder)).map(file => {
      val json = parse(file.file)
      val resource = json.extract[Resource]

      // Create the actors
      val actors: List[Actor] = List(
        Actor(resource.title.label, newTitle)
      )

      // Let each actor annotate
      annotate(resource, actors) match {
        case None => {} // Something failed
        case Some(newResource) => {
          Json.write(newResource)
        }
      }
    })

    // Create the web-service
    webService.shutdown
  }
}
