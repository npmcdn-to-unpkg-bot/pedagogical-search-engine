package agents

import java.io.File

import org.json4s.native.JsonMethods._
import rsc.Types._
import rsc.writers.Json
import rsc.{Resource, Formatters}
import spotlight.WebService
import utils.{Files, Settings}

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {

    val settings = new Settings()
    val webService = new WebService(settings.Spotlight.host, settings.Spotlight.port)

    trait Actor
    case class SingleActor(text: String,
                           fn: (Resource, Spots) => Resource) extends Actor
    case class GroupActor(texts: List[String],
                          fn: (Resource, List[Spots]) => Resource) extends Actor

    case class Work(spotsByGroup: List[Spots], actor: Actor)

    def annotate(resource: Resource, actors: List[Actor]): Option[Resource] = {
      // Annotate each text
      val textsByGroups: List[List[String]] = actors.map(actor => actor match {
        case SingleActor(text, fn) => List(text)
        case GroupActor(texts, fn) => texts
      })
      val oSpots: Option[List[List[Spots]]] = webService.textsToSpotsByGroup(textsByGroups)

      // Let the actors work on the results
      oSpots match {
        case None => None
        case Some(lss) => {
          val work = lss.zip(actors).map(p => Work(p._1, p._2))
          val newResource = work.foldLeft(resource)((rsc, w) => w.actor match {
            case SingleActor(text, fn) => fn(rsc, w.spotsByGroup.head)
            case GroupActor(texts, fn) => fn(rsc, w.spotsByGroup)
          })

          Some(newResource)
        }
      }
    }

    // Create the actors
    def newTitle(resource: Resource, spots: Spots): Resource =
      resource.copy(title = resource.title.copy(oSpots = Some(spots)))

    def newKeywords(resource: Resource, spotsGroups: List[Spots]): Resource = {
      resource.copy(oKeywords = resource.oKeywords match {
        case None => None
        case Some(keywords) => Some(
          keywords.zip(spotsGroups).map(p => {
            val keyword = p._1
            val spots = p._2
            keyword.copy(oSpots = Some(spots))
          })
        )
      })
    }

    def newCategories(resource: Resource, spotsGroups: List[Spots]): Resource = {
      resource.copy(oCategories = resource.oCategories match {
        case None => None
        case Some(categories) => Some(
          categories.zip(spotsGroups).map(p => {
            val category = p._1
            val spots = p._2
            category.copy(oSpots = Some(spots))
          })
        )
      })
    }

    def newDomains(resource: Resource, spots: List[Spots]): Resource =
      resource.copy(oDomains = resource.oDomains match {
        case None => None
        case Some(domains) => Some(
          domains.zip(spots).map(p => {
            val domain = p._1
            val spots = p._2
            domain.copy(oSpots = Some(spots))
          })
        )
    })

    def newSubdomains(resource: Resource, spots: List[Spots]): Resource =
      resource.copy(oSubdomains = resource.oSubdomains match {
        case None => None
        case Some(subdomains) => Some(
          subdomains.zip(spots).map(p => {
            val subdomain = p._1
            val spots = p._2
            subdomain.copy(oSpots = Some(spots))
          })
        )
      })

    // Annotate each resource-file
    Files.explore(new File(settings.Resources.folder)).map(file => {
      val json = parse(file.file)
      val resource = json.extract[Resource]

      // Create the actors
      val oTitleActor = Some(SingleActor(resource.title.label, newTitle))

      val oKeywordLabels = resource.oKeywords.map(keywords => keywords.map(_.label))
      val oKeywordsActor = oKeywordLabels.map(GroupActor(_, newKeywords))

      val oCatergoryLabels = resource.oCategories.map(categories => categories.map(_.label))
      val oCategoriesActor = oCatergoryLabels.map(GroupActor(_, newCategories))

      val oDomainLabels = resource.oDomains.map(domains => domains.map(_.label))
      val oDomainsActor = oDomainLabels.map(GroupActor(_, newDomains))

      val oSubdomainLabels = resource.oSubdomains.map(subdomains => subdomains.map(_.label))
      val oSubdomainsActor = oSubdomainLabels.map(GroupActor(_, newSubdomains))

      val emptyList: List[Actor] = Nil
      val actors: List[Actor] = List(
        oTitleActor,
        oKeywordsActor,
        oCategoriesActor,
        oDomainsActor,
        oSubdomainsActor
      ).foldLeft(emptyList)((l, oActor) => oActor match {
        case None => l
        case Some(actor) => actor::l
      })

      // Let each actor annotate
      annotate(resource, actors) match {
        case None => {} // Something failed
        case Some(newResource) => {
          Json.write(newResource, Some(file.file.getAbsolutePath))
        }
      }
    })

    // Create the web-service
    webService.shutdown
  }
}
