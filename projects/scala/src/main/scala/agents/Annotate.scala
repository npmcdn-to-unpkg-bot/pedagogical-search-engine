package agents

import java.io.File
import java.util.concurrent.Executors

import org.json4s.native.JsonMethods._
import rsc.annotators.{Annotator, Standard}
import rsc.writers.Json
import rsc.{Formatters, Resource}
import spotlight.LazyWebService
import utils.{Files, Logger, Settings}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success

object Annotate extends Formatters {
  def main(args: Array[String]): Unit = {

    val start = System.nanoTime()
    val settings = new Settings()

    // Create the execution pool
    val cores: Int = Runtime.getRuntime().availableProcessors()
    val nbTasks= math.floor(cores * 1).toInt
    val taskPool = ExecutionContext.
      fromExecutor(Executors.newFixedThreadPool(nbTasks))
    val annotatorPool = ExecutionContext.
      fromExecutor(Executors.newFixedThreadPool(nbTasks * 10))

    // Create the web-service
    val ws = new LazyWebService(
      settings.Spotlight.host,
      settings.Spotlight.port,
      annotatorPool
    )

    // For each resource-file
    val futures: List[Future[Boolean]] =
      Files.explore(new File(settings.Resources.folder)).flatMap(file => {
        val friendlyName = file.file.getAbsolutePath
        Logger.info(".. " + friendlyName)

        try {
          // Parse it
          val json = parse(file.file)
          val r = json.extract[Resource]

          // Has the resource been annotated already?
          val annotated = r.oAnnotator match {
            case Some(annotator) => annotator match {
              case Annotator.Standard => true
              case _ => false
            }
            case None => false
          }

          annotated match {
            case true => {
              Logger.info("Skipping: " + friendlyName)
              Nil
            }
            case false => {
              // Annotate it
              val future = Future {
                // 1 Future = annotate 1 resource entirely
                try {
                  val annotate = Standard.annotate(r, ws)(annotatorPool)
                  val newR = Await.result(annotate, 10 days)

                  // Write the resource
                  Json.write(newR, Some(file.file.getAbsolutePath))
                  Logger.info("OK: " + friendlyName)
                  true
                } catch {
                  case e => {
                    val error = e.getClass.getName
                    Logger.error(s"Failed($error): $friendlyName")
                    false
                  }
                }
              }(taskPool)

              List(future)
            }
          }
        } catch {
          case e => {
            Logger.error("Failed to parse: " + friendlyName)
            Nil
          }
        }
    })

    val merged = Future.sequence(futures)(implicitly, taskPool)
    val finalize = merged.andThen({
      case Success(xs) => {
        // Measure execution time
        val stop = System.nanoTime()
        val elapsed = (stop - start) / (1000*1000*1000) // in seconds

        // Log success
        val succeeded = xs.filter(b => b).size
        val failed = xs.size - succeeded
        Logger.info(s"Finished globally: succeeded($succeeded), failed($failed), elapsed(${elapsed}s)")

        // Create the web-service
        ws.shutdown

        // todo: Fix this - it should exit automatically
        System.exit(0)
      }
    })(taskPool)
    Await.result(finalize, 10 days)
  }
}
