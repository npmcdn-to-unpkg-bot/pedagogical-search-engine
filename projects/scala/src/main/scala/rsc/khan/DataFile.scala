package rsc.khan

import java.io.{File}

import rsc.extraction.Data
import utils.Conversions._
import utils.Files.{explore, touch}
import Types.Course

import scala.collection.mutable

class DataFile extends Data[Course] {
  // Define the data-file
  override def in: File = touch(settings.workingDir + "/khan-data-file.json")

  // Extract data from the structure of the folders
  val pages = new File(settings.Resources.Khan.pages)
  val courses = toBuffer(explore(pages).map(rf => Course(rf.parents, rf.file.getAbsolutePath, None)))

  // Methods to implement
  override def data: mutable.Buffer[Course] = courses

  override def copy(o: Course, newStatus: String): Course = o.copy(status = Some(newStatus))

}
