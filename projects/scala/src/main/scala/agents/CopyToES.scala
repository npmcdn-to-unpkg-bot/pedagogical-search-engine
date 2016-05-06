package agents

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import elasticsearch.ResourceWriter
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write
import rsc.{Formatters, Resource}
import utils.{Files, Logger, Settings}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object CopyToES extends App with Formatters {
  //
  val start = System.nanoTime()
  val settings = new Settings()

  // Create the thread pools
  val cores: Int = Runtime.getRuntime.availableProcessors()
  val nbTasks= cores

  val tqExec = Executors.newFixedThreadPool(nbTasks)
  val tasksQueue = ExecutionContext.fromExecutor(tqExec)

  val uqExec = Executors.newFixedThreadPool(10)
  val utilsQueue = ExecutionContext.fromExecutor(uqExec)


  Logger.info(s"Start indexing: #tasks=$nbTasks")

  // Create a monitoring system
  var shutDownMonitoring = false
  val totCounter = new AtomicInteger(0)
  val monitoring = Future {
    while(!shutDownMonitoring) {
      // Sleep a bit
      Thread.sleep(60 * 1000)

      val tot = totCounter.get()

      // Log the stats
      Logger.info(s"Monitoring(rsc/min): tot=$tot")
    }
    Logger.info("Monitoring was shut down")
    true
  }(utilsQueue)

  // Define how each file is processed
  val outputFolder = new File(settings.ElasticSearch.jsonCreation.outputFolder)
  def process(file: File)
  : Future[Any] = {
    // Parse it
    val json = parse(file)
    val r = json.extract[Resource]

    Future {
      Logger.info(s"Process ${file.getName}")

      // Check that the file was not already copied
      val donePath = outputFolder.getAbsolutePath + "/" + file.getName + ".done"
      new File(donePath).exists() match {
        case true => Logger.info(s"Skipping, already copied: ${file.getName}")
        case false =>
          val objects = ResourceWriter.jsonResources(r)
          objects.zipWithIndex.foreach {
            case (obj, index) =>
              val path = outputFolder.getAbsolutePath + "/" + file.getName + s"-frag-$index.json"
              val body = write(obj)
              Files.write(body, path)
          }

          // Indicate that we are done
          Files.write("", donePath)
      }

      totCounter.incrementAndGet()

    }(tasksQueue)
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

  // Shut down java executors
  tqExec.shutdown()
  uqExec.shutdown()
}
