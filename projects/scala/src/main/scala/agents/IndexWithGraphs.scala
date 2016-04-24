package agents

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import org.json4s.native.JsonMethods._
import rsc.annotators.Annotator
import rsc.indexers.{Graph, Indexer}
import rsc.writers.Json
import rsc.{Formatters, Resource}
import utils.{Logger, Settings}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object IndexWithGraphs extends App with Formatters {
  //
  val start = System.nanoTime()
  val settings = new Settings()

  // Create the thread pools
  val cores: Int = Runtime.getRuntime().availableProcessors()
  val nbTasks= settings.Indices.Indexation.nbTasks
  val tasksQueue = ExecutionContext.
    fromExecutor(Executors.newFixedThreadPool(nbTasks))
  val indexerQueue = ExecutionContext.
    fromExecutor(Executors.newFixedThreadPool(nbTasks * 10))
  val utilsQueue = ExecutionContext.
    fromExecutor(Executors.newFixedThreadPool(10))

  Logger.info(s"Start indexing: #tasks=${nbTasks}")

  // Create a monitoring system
  var shutDownMonitoring = false

  val totCounter = new AtomicInteger(0)
  val okCounter = new AtomicInteger(0)
  val naCounter = new AtomicInteger(0) // na = Not annotated
  val alreadyCounter = new AtomicInteger(0)
  val rejectedCounter = new AtomicInteger(0)
  val errorCounter = new AtomicInteger(0)

  val totCounterLT = new AtomicInteger(0) // LT = Long term
  val okCounterLT = new AtomicInteger(0)
  val naCounterLT = new AtomicInteger(0)
  val alreadyCounterLT = new AtomicInteger(0)
  val rejectedCounterLT = new AtomicInteger(0)
  val errorCounterLT = new AtomicInteger(0)

  val monitoring = Future {
    while(!shutDownMonitoring) {
      // Sleep a bit
      Thread.sleep(60 * 1000)

      // Reset counters
      val tot = totCounter.getAndSet(0)
      val ok = okCounter.getAndSet(0)
      val na = naCounter.getAndSet(0)
      val already = alreadyCounter.getAndSet(0)
      val rejected = rejectedCounter.getAndSet(0)
      val error = errorCounter.getAndSet(0)

      // Log for the long-term stats
      val totLT = totCounterLT.addAndGet(tot)
      val okLT = okCounterLT.addAndGet(ok)
      val naLT = naCounterLT.addAndGet(na)
      val alreadyLT = alreadyCounterLT.addAndGet(already)
      val rejectedLT = rejectedCounterLT.addAndGet(rejected)
      val errorLT = errorCounterLT.addAndGet(error)

      // Log the stats
      Logger.info(s"Monitoring(rsc/min): tot=${tot}, ok=${ok}, rejected=${rejected}, already=${already}, na=${na}, error=${error}")
      Logger.info(s"Statistics(#rsc): tot=${totLT}, ok=${okLT}, rejected=${rejectedLT}, already=${alreadyLT}, na=${naLT}, error=${errorLT}")
    }
    true
  }(utilsQueue)

  // Define how the resources are processed
  def process(file: File): Future[Any] = {
    val name = file.getAbsolutePath
    val indexer = new Graph(indexerQueue)

    try {
      // Parse it
      val json = parse(file)
      val r = json.extract[Resource]

      // Was it already annotated?
      val annotated = r.oAnnotator match {
        case None => false
        case Some(annotator) => annotator match {
          case Annotator.Standard => true
          case _ => false
        }
      }

      // Was it already indexed?
      val indexed = r.oIndexer match {
        case None => false
        case Some(i) => i match {
          case Indexer.Graph => true
          case _ => false
        }
      }

      // Index it
      (annotated, indexed) match {
        case (false, _) => {
          Logger.info(s"Skipping - Resource not annotated: $name")
          totCounter.incrementAndGet()
          naCounter.incrementAndGet()
          Future.successful()
        }
        case (_ ,true) => {
          Logger.info(s"Skipping - Resource already indexed: $name")
          totCounter.incrementAndGet()
          alreadyCounter.incrementAndGet()
          Future.successful()
        }
        case  _ => {
          Future {
            Logger.info(s"Processing ${file.getAbsolutePath}")

            // [TL;DR] 1 Future = index 1 resource entirely
            //
            // We only terminate when the resource indices are computed and written.
            //
            // This enforces that the maximum number of resources that
            // are indexed in parallel does not exceed the "tasksQueue" pool size.
            // i.e. Otherwise, we might end up computing the indices of all the resources
            // in parallel and get out of memory since the intermediate results are
            // stored in memory.
            //
            // Note that: The computation itself can be fragmented into
            // futures and cleverly scheduled such that all CPU is used etc..
            // It is the purpose of the "indexerQueue"
            try {
              // Launch the indexation for the current resource
              // in [indexerQueue]
              val index = indexer.index(r)

              Await.result(index, 10 days) match {
                case Some(newR) => {
                  Json.write(newR, Some(file.getAbsolutePath))
                  Logger.info(s"OK: $name")
                  totCounter.incrementAndGet()
                  okCounter.incrementAndGet()
                }
                case None => {
                  Logger.error(s"Rejected: $name")
                  totCounter.incrementAndGet()
                  rejectedCounter.incrementAndGet()
                }
              }
            } catch {
              case e: Throwable => {
                val error = e.getClass.getName
                e.printStackTrace()
                Logger.error(s"Error($error): $name")
                totCounter.incrementAndGet()
                errorCounter.incrementAndGet()
              }
            }
          }(tasksQueue)
        }
      }
    } catch {
      case e => {
        Logger.info(s"Error: Cannot parse: $name")
        totCounter.incrementAndGet()
        errorCounter.incrementAndGet()
        Future.successful()
      }
    }
  }

  // Process each resource-file
  var lastIndexation: Future[Any] = Future.successful()

  val directory = new File(settings.Resources.folder)
  val it = org.apache.commons.io.FileUtils.iterateFiles(
    directory,
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

  // Display some more stats
  val stop = System.nanoTime()
  val elapsed = (stop - start) / (1000*1000*1000) // in seconds
  Logger.info(s"Finished: elapsed=${elapsed}s")
  System.exit(1)
}
