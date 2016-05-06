package agents

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import elasticsearch.ResourceWriter
import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s.native.Serialization.write
import rsc.{Formatters, Resource}
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import utils.{Files, Logger, Settings}

import scala.concurrent.duration._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object CopyToES extends App with Formatters {
  //
  val start = System.nanoTime()
  val settings = new Settings()

  // Create the thread pools
  val cores: Int = Runtime.getRuntime.availableProcessors()
  val nbTasks= cores

  val tqExec = Executors.newFixedThreadPool(nbTasks)
  val tasksQueue = ExecutionContext.fromExecutor(tqExec)

  val iqExec = Executors.newFixedThreadPool(nbTasks * 10)
  val importQueue = ExecutionContext.fromExecutor(iqExec)

  val uqExec = Executors.newFixedThreadPool(10)
  val utilsQueue = ExecutionContext.fromExecutor(uqExec)

  // Create the http actors
  implicit private val system = ActorSystem("es-import")
  import system.dispatcher
  val timeoutMs = 10*60*1000
  val timeout = timeoutMs.milliseconds

  val ip = settings.ElasticSearch.ip
  val port = settings.ElasticSearch.port
  val esIndex = settings.ElasticSearch.esIndex
  val esType = settings.ElasticSearch.esType
  val url = s"http://$ip:$port/_bulk"

  val pipeline: HttpRequest => Future[HttpResponse] = (
    addHeader("Accept", "application/json")
      ~> sendReceive(implicitly, implicitly, futureTimeout = timeout)
    )

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
  val esComplement: JObject =
    "index" -> (
      ("_index" -> esIndex) ~
        ("_type" -> esType)
      )
  def process(file: File)
  : Future[Any] = {
    Future {
      Logger.info(s"Process ${file.getName}")

      // Parse it
      val json = parse(file)
      val r = json.extract[Resource]

      // Check that the file was not already copied
      val donePath = outputFolder.getAbsolutePath + "/" + file.getName + ".done"
      new File(donePath).exists() match {
        case true => Logger.info(s"Skipping, already copied: ${file.getName}")
        case false =>

          val objects = ResourceWriter.jsonResources(r)
          val body = objects.map(o => write(esComplement ~ o)).mkString("\n")

          // The request will be executed in the import queue
          val request = pipeline(Post(url, body))

          //
          Await.result(request, Duration.Inf)

          request.onComplete {
            case Success(response) =>
              // Indicate that we are done
              Files.write("", donePath)

              // Log the success
              println(s"Successfully posted ${file.getName}")
              totCounter.incrementAndGet()

            case Failure(e) =>
              e.printStackTrace()
              totCounter.incrementAndGet()
          }(importQueue)
      }

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

  Logger.info(s"Start indexing: #tasks=$nbTasks")
  while(it.hasNext) {
    val file = it.next().asInstanceOf[File]
    lastIndexation = process(file)
  }

  // Wait for the last task
  Await.result(lastIndexation, Duration.Inf)
  shutDownMonitoring = true
  Await.result(monitoring, Duration.Inf)

  // Shut down java executors
  iqExec.shutdown()
  tqExec.shutdown()
  uqExec.shutdown()
  system.shutdown()
}
