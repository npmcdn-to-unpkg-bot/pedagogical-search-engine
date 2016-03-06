package mit

import java.io.File

import mit.Types.{Course, Courses}
import rtoc.Data
import utils.Conversions.toBuffer

import scala.collection.mutable

class DataFile(in: File) extends Data[Course](in) {

  // Extract each article
  val courses = toBuffer(parsed.extract[Courses])

  // Methods to implement
  override def data: mutable.Buffer[Course] = courses

  override def copy(o: Course, newStatus: String): Course = o.copy(status = Some(newStatus))
}
