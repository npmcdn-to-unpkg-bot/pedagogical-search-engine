package rsc.safaribooks

import java.io.File

import rsc.Data
import Types.Book
import utils.Files.explore
import utils.Conversions.toBuffer

import scala.collection.mutable

class DataFile(in: File, pagesFolder: File) extends Data[Book](in: File) {
  // Extract the books
  val books = toBuffer(explore(pagesFolder).map(rf => Book(rf.file.getAbsolutePath, None)))

  // Methods to implement
  override def data: mutable.Buffer[Book] = books

  override def copy(o: Book, newStatus: String): Book = o.copy(status = Some(newStatus))
}
