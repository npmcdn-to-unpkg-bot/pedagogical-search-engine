package agents

import rsc.coursera.Types.Course
import rsc.coursera.{DataFile, Factory}
import rsc.extraction.Worker
import rsc.writers.Json

object Coursera {
  def main(args: Array[String]): Unit = {
    val factory = new Factory
    val data = new DataFile(alreadyRan = false)
    val worker = new Worker[Course](data, factory, Json)
    worker.work()
  }
}
