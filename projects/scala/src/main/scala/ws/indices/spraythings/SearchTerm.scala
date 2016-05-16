package ws.indices.spraythings

import utils.StringUtils

import scala.util.hashing.MurmurHash3

case class SearchTerm(label: String, uri: Option[String]) {

  // It is important to normalize, otherwise we could register
  // two different searches for "A" and "a"
  def normalize(): SearchTerm =
    SearchTerm(label.trim().toLowerCase(),
      uri.map(StringUtils.normalizeUri))
}

object SearchTerm {
  def searchHash(searchTerms: TraversableOnce[SearchTerm])
  : Int = {
    val elements = searchTerms.map {
      case SearchTerm(label, uri) => uri match {
        case None => label
        case Some(x) => x
      }
    }
    MurmurHash3.unorderedHash(elements)
  }

  def validationSkim(searchTerms: TraversableOnce[SearchTerm])
  : TraversableOnce[SearchTerm] = searchTerms.flatMap {
    case x =>
      val normalized = x.normalize()

      if(normalized.label.length > 0 && normalized.uri.getOrElse(".").length > 0) {
        List(x)
      } else {
        Nil
      }
  }

  def uris(searchTerms: TraversableOnce[SearchTerm])
  : TraversableOnce[String] =
    searchTerms.flatMap {
      case SearchTerm(_, Some(uri)) => List(uri)
      case _ => Nil
    }

  def searchText(searchTerms: TraversableOnce[SearchTerm])
  : String = searchTerms.map(_.label).mkString(" ")
}
