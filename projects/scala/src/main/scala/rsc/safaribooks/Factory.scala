package rsc.safaribooks

import java.io.File

import org.json4s.JsonAST.JString
import rsc.Resource
import rsc.safaribooks.Types.Book
import Types.Book
import utils.Conversions.{hash, normalize}
import utils.Logger

class Factory(outputFolder: File) extends rsc.Factory[Book] {
  override def getOrFail(book: Book): Resource = {
    // Open page
    val doc = open(book.path)

    // Metadata
    val metaEl = doc match {
      case layouts.metadata.Simple(re) => re
    }

    // TOC
    val tocEl = doc match {
      case layouts.toc.Simple(re) => re
    }

    // Create ressource
    val folderPath = outputFolder.getAbsolutePath + "/safari"
    val title = metaEl.oMetadata match {
      case Some(m) => m \\ "title" match {
        case JString(title) => normalize(title)
      }
    }
    val name = title + "-" + hash(book.path)
    val mergedEl = metaEl.mergeWith(tocEl)
    Logger.info(s"$name: OK")
    mergedEl.resource(folderPath, name)
  }
}
