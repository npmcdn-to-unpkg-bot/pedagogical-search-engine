package ws.indices.spraythings

import scala.util.hashing.MurmurHash3

case class SearchTerm(label: String, uri: Option[String]) {

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

  def uris(searchTerms: TraversableOnce[SearchTerm])
  : TraversableOnce[String] =
    searchTerms.flatMap {
      case SearchTerm(_, Some(uri)) => List(uri)
      case _ => Nil
    }

  def searchText(searchTerms: TraversableOnce[SearchTerm])
  : String = searchTerms.map(_.label).mkString(" ")
}
