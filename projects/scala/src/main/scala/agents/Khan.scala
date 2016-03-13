package agents

import rsc.extraction.Worker
import rsc.khan.Types.Course
import rsc.khan.{DataFile, Factory}
import rsc.writers.Json

object Khan {
  def main(args: Array[String]): Unit = {
      // Create the worker
      val factory = new Factory()
      val data = new DataFile()
      val worker = new Worker[Course](data, factory, Json)
      worker.work()
  }
}
