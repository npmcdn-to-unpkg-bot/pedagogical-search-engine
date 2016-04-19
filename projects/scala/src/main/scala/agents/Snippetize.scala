package agents

import java.io.File
import java.util.concurrent.Executors

import org.json4s.native.JsonMethods._
import rsc.indexers.Indexer
import rsc.writers.Json
import rsc.{Formatters, Resource}
import utils.{Files, Logger, Settings}
import rsc.snippets.Simple

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success

object Snippetize extends Formatters {
  def main(args: Array[String]): Unit = {

    // Initialize
    val start = System.nanoTime()
    val settings = new Settings()
    val snippetizer = new Simple()

    // Create the thread pools
    val cores: Int = Runtime.getRuntime().availableProcessors()
    val tasksQueue = ExecutionContext.
      fromExecutor(Executors.newFixedThreadPool(cores))

    Logger.info(s"Start indexing: #tasks=#cores=${cores}")

    // For each resource-file
    val futures = Files.explore(
      new File(settings.Resources.folder)).flatMap(file => {
      val name = file.file.getAbsolutePath

      try {
        // Parse it
        val json = parse(file.file)
        val r = json.extract[Resource]

        // Was it already indexed?
        val indexed = r.oIndexer match {
          case None => false
          case Some(i) => i match {
            case Indexer.Graph => true
            case _ => false
          }
        }

        // Test minimal conditions
        indexed match {
          case false => {
            Logger.info(s"Skipping - Resource not indexed: $name")
            Nil
          }
          case  true => {
            val future = Future {
              Logger.info(s"Processing ${file.file.getAbsolutePath}")

              try {
                //  Snippetize
                val newR = snippetizer.snippetize(r)

                // Write the result
                Json.write(newR, Some(file.file.getAbsolutePath))
                Logger.info(s"OK: $name")

                // Everything went OK
                true
              } catch {
                // In something went wrong
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
