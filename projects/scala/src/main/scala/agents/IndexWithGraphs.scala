package agents

import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import org.json4s.native.JsonMethods._
import rsc.annotators.Annotator
import rsc.indexers.{Graph, Indexer}
import rsc.writers.Json
import rsc.{Formatters, Resource}
import utils.{Files, Logger, Settings}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success

object IndexWithGraphs extends Formatters {
  def main(args: Array[String]): Unit = {

    val start = System.nanoTime()
    val settings = new Settings()

    // Create the thread pools
    val cores: Int = Runtime.getRuntime().availableProcessors()
    val nbTasks= settings.Indices.Import.nbTasks
    val tasksQueue = ExecutionContext.
      fromExecutor(Executors.newFixedThreadPool(nbTasks))
    val indexerQueue = ExecutionContext.
      fromExecutor(Executors.newFixedThreadPool(nbTasks * 10))

    Logger.info(s"Start indexing: #tasks=${nbTasks}")

    // Create a monitoring system
    val totCounter = new AtomicInteger(1)
    val okCounter = new AtomicInteger(0)
    val naCounter = new AtomicInteger(0) // na = Not annotated
    val alreadyCounter = new AtomicInteger(0)
    val rejectedCounter = new AtomicInteger(0)
    val errorCounter = new AtomicInteger(0)

    val totCounterLT = new AtomicInteger(-1) // LT = Long term
    val okCounterLT = new AtomicInteger(0)
    val naCounterLT = new AtomicInteger(0)
    val alreadyCounterLT = new AtomicInteger(0)
    val rejectedCounterLT = new AtomicInteger(0)
    val errorCounterLT = new AtomicInteger(0)

    val monitoring = Future {
      while(totCounter.get() > 0) {
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

        // Sleep a bit
        Thread.sleep(60 * 1000)
      }
      true
    }(tasksQueue)

    // For each resource-file
    val futures = Files.explore(
      new File(settings.Resources.folder)).flatMap(file => {
      val name = file.file.getAbsolutePath
      val indexer = new Graph(indexerQueue)

      try {
        // Parse it
        val json = parse(file.file)
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
            Nil
          }
          case (_ ,true) => {
            Logger.info(s"Skipping - Resource already indexed: $name")
            totCounter.incrementAndGet()
            alreadyCounter.incrementAndGet()
            Nil
          }
          case  _ => {
            val future = Future {
              Logger.info(s"Processing ${file.file.getAbsolutePath}")

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
                    Json.write(newR, Some(file.file.getAbsolutePath))
                    Logger.info(s"OK: $name")
                    totCounter.incrementAndGet()
                    okCounter.incrementAndGet()
                    true
                  }
                  case None => {
                    Logger.error(s"Rejected: $name")
                    totCounter.incrementAndGet()
                    rejectedCounter.incrementAndGet()
                    false
                  }
                }
              } catch {
                case e: Throwable => {
                  val error = e.getClass.getName
                  e.printStackTrace()
                  Logger.error(s"Error($error): $name")
                  totCounter.incrementAndGet()
                  errorCounter.incrementAndGet()
                  false
                }
              }
            }(tasksQueue)

            List(future)
          }
        }
      } catch {
        case e => {
          Logger.info(s"Error: Cannot parse: $name")
          totCounter.incrementAndGet()
          errorCounter.incrementAndGet()
          Nil
        }
      }
    })

    val merge = Future.sequence(monitoring::futures)(implicitly, tasksQueue)
    val finalize = merge.andThen({
      case Success(xs) => {
        // Measure execution time
        val stop = System.nanoTime()
        val elapsed = (stop - start) / (1000*1000*1000) // in seconds

        // Log success
        val succeeded = xs.filter(b => b).size
        val failed = xs.size - succeeded
        Logger.info(s"Finished globally: succeeded($succeeded), failed($failed), elapsed(${elapsed}s)")

        // todo: Fix this - it should exit automatically
        System.exit(0)
      }
    })(tasksQueue)

    Await.result(finalize, 10 days)
  }
}
