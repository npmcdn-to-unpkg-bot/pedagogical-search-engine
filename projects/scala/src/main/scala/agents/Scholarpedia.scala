package agents

import rsc.extraction.Worker
import rsc.scholarpedia.Types.Article
import rsc.scholarpedia.{DataFile, Factory}
import rsc.writers.Json

object Scholarpedia {
  def main(args: Array[String]): Unit = {
    val factory = new Factory()
    val data = new DataFile()
    val worker = new Worker[Article](data, factory, Json)
    worker.work()
  }
}
