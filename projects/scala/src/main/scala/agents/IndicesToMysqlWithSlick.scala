package agents

import java.io.File
import java.util.concurrent.Executors

import org.json4s.native.JsonMethods._
import rsc.{Formatters, Resource}
import rsc.indexers.Indexer
import utils.{Files, Logger, Settings}
import rsc.importers.Importer.{Importer, SlickMysql}
import rsc.writers.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success

object IndicesToMysqlWithSlick extends App with Formatters {

  // Monitor progress
  val start = System.nanoTime()

  // Load project-related settings
  val settings = new Settings()

  // Create the thread pools
  val cores: Int = Runtime.getRuntime().availableProcessors()
  val nbTasks= math.floor(cores * 4).toInt
  val tasksQueue = ExecutionContext.
    fromExecutor(Executors.newFixedThreadPool(nbTasks))
  val importerQueue = ExecutionContext.
    fromExecutor(Executors.newFixedThreadPool(nbTasks * 10))

  // The indices are imported through an importer
  lazy val importer = ??? // todo: use importerQueue

  // Explore the resources
  val futures = Files.explore(
    new File(settings.Resources.folder)).flatMap(file => {
    // Open a resource
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

      // Was it already imported with this importer?
      val imported = r.oImporters match {
        case None => false
        case Some(xs) => xs.contains(SlickMysql) match {
          case true => true
          case false => false
        }
      }

      // Import the indices only if the resource is indexed
      (imported, indexed) match {
        case (_, false) => {
          Logger.info(s"Skipping - Resource is not yet indexed: $name")
          Nil
        }
        case (true, _) => {
          Logger.info(s"Skipping - Resource was already imported: $name")
          Nil
        }
        case (false, true) => {
          val future = Future {
            Logger.info(s"Processing ${file.file.getAbsolutePath}")

            // Import the indices of the resource
            try {
              // Launch the import for the current resource
              // in [importerQueue]
              // todo: importer.import(r)
              val process: Future[Option[Resource]] = Future { Some(r) }(importerQueue)

              Await.result(process, Duration.Inf) match {
                case Some(newR) => {
                  // todo: move these lines into the importer
                  val existingImporters = r.oImporters match {
                    case None => Nil
                    case Some(xs) => xs
                  }
                  val newOImporters: Option[List[Importer]] =
                    Some(SlickMysql::existingImporters)

                  // Update the resource file
                  Json.write(
                    newR.copy(oImporters = newOImporters),
                    Some(file.file.getAbsolutePath)
                  )
                  Logger.info(s"OK: $name")
                  true
                }
                case None => {
                  Logger.error(s"Cannot import: $name")
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

  // Merge the tasks
  val merged = Future.sequence(futures)(implicitly, tasksQueue)
  val finalized = merged.andThen({
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

  Await.result(finalized, Duration.Inf)
}
