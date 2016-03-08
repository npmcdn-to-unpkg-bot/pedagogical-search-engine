package khan

import java.io.{File}

import utils.Conversions._
import utils.Files.explore
import khan.Types.Course
import rsc.Data

import scala.collection.mutable

class DataFile(in: File, courseFolder: File) extends Data[Course](in: File) {
  // Extract data from the structure of the folders
  val courses = toBuffer(explore(courseFolder).map(rf => Course(rf.parents, rf.file.getAbsolutePath, None)))

  // Methods to implement
  override def data: mutable.Buffer[Course] = courses

  override def copy(o: Course, newStatus: String): Course = o.copy(status = Some(newStatus))
}
