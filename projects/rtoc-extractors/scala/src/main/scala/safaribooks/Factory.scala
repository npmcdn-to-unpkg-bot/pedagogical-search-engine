package safaribooks

import java.io.File

import org.jsoup.nodes.Document
import rsc.Resource
import safaribooks.Types.Book
import utils.Conversions.hash

class Factory(outputFolder: File) extends rsc.Factory[Book] {
  override def getOrFail(book: Book): Resource = {
    // Open page
    val doc = open(book.path)

    // TOC
    val tocEl = doc match {
      case layouts.toc.Simple(re) => re
    }

    // Create ressource
    val folderPath = outputFolder.getAbsolutePath + "/safari"
    val name = hash(book.path)
    tocEl.resource(folderPath, name)
  }
}
