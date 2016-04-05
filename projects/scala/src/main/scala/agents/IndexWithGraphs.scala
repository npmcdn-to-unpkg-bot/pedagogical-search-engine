package agents

import java.io.File
import java.util.concurrent.Executors

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
    val nbTasks= math.floor(cores * 3).toInt
    val tasksQueue = ExecutionContext.
      fromExecutor(Executors.newFixedThreadPool(nbTasks))
    val indexerQueue = ExecutionContext.
      fromExecutor(Executors.newFixedThreadPool(nbTasks * 10))

    Logger.info(s"Start indexing: #tasks=${nbTasks}")

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
            Nil
          }
          case (_ ,true) => {
            Logger.info(s"Skipping - Resource already indexed: $name")
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
                    true
                  }
                  case None => {
                    Logger.error(s"Cannot index: $name")
                    false
                  }
                }
              } catch {
                case e: Throwable => {
                  val error = e.getClass.getName
                  e.printStackTrace()
                  Logger.error(s"Failed($error): $name")
                  false
                }
              }
            }(tasksQueue)

            List(future)
          }
        }
      } catch {
        case e => {
          Logger.info(s"Cannot parse: $name")
          Nil
        }
      }
    })

    val merge = Future.sequence(futures)(implicitly, tasksQueue)
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
