package rsc.coursera

import rsc.Resource
import rsc.Resource._
import rsc.Types.Nodes
import rsc.attributes.{Level, Source}
import rsc.coursera.Types.{Domain, Course}
import rsc.coursera.layouts.Inline
import rsc.toc.{Node, Toc}
import utils.Logger

class Factory extends rsc.extraction.Factory[Course]() {

  override def getOrFail(course: Course): Resource = {
    // Parse
    val doc = openWeird(settings.Resources.Coursera.pages, course.localPath)

    // Match the page layout
    val href = course.href
    doc match {
      case layouts.Simple(p) => {
        Logger.debug(s"Simple-layout: $href")
        process(course, p)
      }
      case Inline(p) => {
        Logger.debug(s"Inline-layout: $href")
        process(course, p)
      }
    }
  }

  def process(course: Course, toc: Toc): Resource = {
    // Create the metadata
    val partnersV = course.partner match {
      case Some(partner) => Some(partner::Nil)
      case None => None
    }
    val source = Source.Coursera
    val title = course.label

    val level = Level.University
    val href = course.href
    val miniature = course.localImg
    val screenshot = course.screenshot
    val oDomain = course.domain
    val oSubdomain = course.subdomain

    // Extend toc with domain, subdomain
    def addDomain(nodes: Nodes): Nodes = oDomain match {
      case Some(domain) => new Node(domain, nodes)::Nil
      case None => nodes
    }

    def addSubdomain(nodes: Nodes): Nodes = oSubdomain match {
      case Some(subdomain) => new Node(subdomain, nodes)::Nil
      case None => nodes
    }

    def completeNodes(nodes: Nodes): Nodes = addDomain(addSubdomain(nodes))

    val extendedToc = Toc(completeNodes(toc.nodes))

    // Create the resource
    val oDomains = oDomain.map(rsc.attributes.Domain(_)::Nil)
    val oSubdomains = oSubdomain.map(rsc.attributes.Subdomain(_)::Nil)
    Resource(source, title,
      oLevel = Some(level),
      oHref = Some(href),
      oMiniature = Some(miniature),
      oScreenshot = Some(screenshot),
      oDomains = oDomains,
      oSubdomains = oSubdomains,
      oTocs = Some(extendedToc::Nil))
  }
}
