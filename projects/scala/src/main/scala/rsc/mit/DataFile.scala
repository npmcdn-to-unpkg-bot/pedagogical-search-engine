package rsc.mit

import java.io.File

import Types.{Course, Courses}
import rsc.Data
import rsc.mit.Types.Course
import utils.Conversions.toBuffer

import scala.collection.mutable

class DataFile(in: File) extends Data[Course](in) {

  // Extract each article
  val courses = toBuffer(parsed.extract[Courses])

  // Methods to implement
  override def data: mutable.Buffer[Course] = courses

  override def copy(o: Course, newStatus: String): Course = o.copy(status = Some(newStatus))
}
