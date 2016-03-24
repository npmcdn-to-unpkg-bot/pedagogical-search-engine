package rsc.annotators

import rsc.Resource
import rsc.Types._
import spotlight.WebService

object Standard {
  def annotate(r: Resource, ws: WebService): Option[Resource] = {
    try {
      Some(newResource(r, ws))
    } catch {
      case e => None
    }
  }

  private def newResource(r: Resource, ws: WebService): Resource = {

    //
    val titleWork: List[(Object, String)] = (r.title -> r.title.label)::Nil

    //
    val keyWork: List[(Object, String)] = r.oKeywords match {
      case None => Nil
      case Some(keywords) => keywords.map {
        case keyword => (keyword -> keyword.label)
      }
    }

    //
    val catWork: List[(Object, String)] = r.oCategories match {
      case None => Nil
      case Some(categories) => categories.map {
        case category => (category -> category.label)
      }
    }

    //
    val domWork: List[(Object, String)] = r.oDomains match {
      case None => Nil
      case Some(domains) => domains.map {
        case domain => (domain -> domain.label)
      }
    }

    //
    val subdomWork: List[(Object, String)] = r.oSubdomains match {
      case None => Nil
      case Some(subdomains) => subdomains.map {
        case subdomain => (subdomain -> subdomain.label)
      }
    }

    //
    val desWork: List[(Object, String)] = r.oDescriptions match {
      case None => Nil
      case Some(descriptions) => descriptions.map {
        case description => (description -> description.text)
      }
    }

    //
    def getNodesWork(nodes: Nodes): List[(Object, String)] =
      nodes.map(node => {
        val labelWork = (node -> node.label)
        labelWork::getNodesWork(node.children)
      }) match {
        case Nil => Nil
        case xs => xs reduce (_ ::: _)
      }

    val nodeWork: List[(Object, String)] = r.oTocs match {
      case None => Nil
      case Some(tocs) => tocs.map(toc => {
        getNodesWork(toc.nodes)
      }) match {
        case Nil => Nil
        case xs => xs reduce (_ ::: _)
      }
    }

    //
    val work: List[(Object, String)] =
      titleWork ::: keyWork ::: catWork ::: domWork ::: subdomWork ::: desWork ::: nodeWork

    //
    val results: Map[Object, Spots] = ws.annotateTogether(
      work.map {
        case (obj, text) => text
      }
    ) match {
      case Some(spotsList) => work.map {
        case (obj, _) => obj
      }.zip(spotsList).toMap
    }

    // Function to annotate the nodes recursively
    def annotateNodes(nodes: Nodes): Nodes = nodes.map(node => {
      val spots = results(node)
      node.copy(oSpots = Some(spots), children = annotateNodes(node.children))
    })

    // Construct new elements
    val newTitle = r.title.copy(oSpots = Some(results(r.title)))

    val newOKeywords = r.oKeywords.map(keywords => {
      keywords.map(keyword => {
        keyword.copy(oSpots = Some(results(keyword)))
      })
    })

    val newOCategories = r.oCategories.map(categories => {
      categories.map(category => {
        category.copy(oSpots = Some(results(category)))
      })
    })

    val newODomains = r.oDomains.map(domains => {
      domains.map(domain => {
        domain.copy(oSpots = Some(results(domain)))
      })
    })

    val newOSubdomains = r.oSubdomains.map(subdomains => {
      subdomains.map(subdomain => {
        subdomain.copy(oSpots = Some(results(subdomain)))
      })
    })

    val newOTocs = r.oTocs.map(tocs => {
      tocs.map(toc => {
        toc.copy(nodes = annotateNodes(toc.nodes))
      })
    })

    val newODescriptions = r.oDescriptions.map(descriptions => {
      descriptions.map(description => {
        description.copy(oSpots = Some(results(description)))
      })
    })

    // Create the new resource
    r.copy(
      oAnnotator = Some(Annotator.Standard),
      title = newTitle,
      oKeywords = newOKeywords,
      oCategories = newOCategories,
      oDomains = newODomains,
      oSubdomains = newOSubdomains,
      oTocs = newOTocs,
      oDescriptions = newODescriptions
    )
  }
}
