package agents

import rsc.extraction.Worker
import rsc.safaribooks.Types.Book
import rsc.safaribooks.{DataFile, Factory}
import rsc.writers.Json

object Safaribooks {
  def main(args: Array[String]): Unit = {
    val factory = new Factory()
    val data = new DataFile()
    val worker = new Worker[Book](data, factory, Json)
    worker.work()
  }
}
