package agents

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import org.json4s.native.JsonMethods._
import rsc.{Formatters, Resource}
import utils.{Logger, Settings}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object CopyToES extends App with Formatters {
  //
  val start = System.nanoTime()
  val settings = new Settings()

  // Create the thread pools
  val cores: Int = Runtime.getRuntime().availableProcessors()
  val nbTasks= cores
  val tasksQueue = ExecutionContext.
    fromExecutor(Executors.newFixedThreadPool(nbTasks))
  val utilsQueue = ExecutionContext.
    fromExecutor(Executors.newFixedThreadPool(10))

  Logger.info(s"Start indexing: #tasks=${nbTasks}")

  // Create a monitoring system
  var shutDownMonitoring = false
  val totCounter = new AtomicInteger(0)
  val monitoring = Future {
    while(!shutDownMonitoring) {
      // Sleep a bit
      Thread.sleep(60 * 1000)

      val tot = totCounter.get()

      // Log the stats
      Logger.info(s"Monitoring(rsc/min): tot=${tot}")
    }
    true
  }(utilsQueue)

  // Define how each file is processed
  val outputFolder = new File(settings.ElasticSearch.jsonCreation.outputFolder)
  def process(file: File)
  : Future[Any] = {
    // Parse it
    val json = parse(file)
    val r = json.extract[Resource]

    // todo: implement
    println(s"process ${file.getName}")

    // todo: delete
    Future.successful("yes")
  }

  // Iterate over the resource files
  var lastIndexation: Future[Any] = Future.successful()
  val inputFolder = new File(settings.ElasticSearch.jsonCreation.inputFolder)
  val it = org.apache.commons.io.FileUtils.iterateFiles(
    inputFolder,
    Array("json"),
    true
  )

  while(it.hasNext) {
    val file = it.next().asInstanceOf[File]
    lastIndexation = process(file)
  }

  // Wait for the last task
  Await.result(lastIndexation, Duration.Inf)
  shutDownMonitoring = true
  Await.result(monitoring, Duration.Inf)
}
