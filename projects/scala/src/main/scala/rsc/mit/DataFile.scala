package rsc.mit

import java.io.File

import rsc.extraction.Data
import rsc.mit.Types.{Course, Courses}
import utils.Conversions.toBuffer

import scala.collection.mutable

class DataFile extends Data[Course] {

  // Extract each article
  val courses = toBuffer(parsed.extract[Courses])

  // Methods to implement
  override def data: mutable.Buffer[Course] = courses

  override def copy(o: Course, newStatus: String): Course = o.copy(status = Some(newStatus))

  override def in: File = new File(settings.Resources.Mit.data)
}
