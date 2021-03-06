package rsc.safaribooks

import rsc.Resource
import rsc.Resource._
import rsc.attributes.{Title, Source}
import rsc.safaribooks.Types.Book

class Factory extends rsc.extraction.Factory[Book] {
  override def getOrFail(book: Book): Resource = {
    // Open page
    val doc = open(book.path)

    // TOC
    val toc = doc match {
      case layouts.toc.Simple(re) => re
    }

    // Metadata
    val meta = doc match {
      case layouts.metadata.Simple(re) => re
    }
    val source = Source.Safari
    val title = meta.title

    val publishers = meta.publisher::Nil

    Resource(
      source = source,
      title = Title(title),
      oTocs = Some(List(toc)),
      oPublishers = Some(publishers))
  }
}
