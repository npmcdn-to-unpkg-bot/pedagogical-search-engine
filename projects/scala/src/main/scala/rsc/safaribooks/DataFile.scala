package rsc.safaribooks

import java.io.File

import rsc.extraction.Data
import rsc.safaribooks.Types.Book
import utils.Conversions.toBuffer
import utils.Files._

import scala.collection.mutable

class DataFile extends Data[Book] {
  // Extract the books
  val pages = new File(settings.Resources.Safari.pages)
  val books = toBuffer(explore(pages).map(rf => Book(rf.file.getAbsolutePath, None)))

  // Methods to implement
  override def data: mutable.Buffer[Book] = books

  override def copy(o: Book, newStatus: String): Book = o.copy(status = Some(newStatus))

  override def in: File = touch(settings.workingDir + "/safari-data-file.json")
}
