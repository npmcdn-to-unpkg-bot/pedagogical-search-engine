package agents

import rsc.extraction.Worker
import rsc.mit.Types.Course
import rsc.mit.{DataFile, Factory}
import rsc.writers.Json

object Mit {
  def main(args: Array[String]): Unit = {
    val factory = new Factory()
    val data = new DataFile()
    val worker = new Worker[Course](data, factory, Json)
    worker.work()
  }
}
