package evaluation.manual

import java.io.File

import agents.helpers.FileExplorer
import evaluation.manual.json.{Annotation, AnnotationElement, Formatters}
import evaluation.manual.task.{Graph2, Statistics}
import org.json4s.native.JsonMethods._
import spotlight.LazyWebService
import utils.Settings

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object Evaluate extends App with Formatters {
  //
  val settings = new Settings()
  val annotatorWs = new LazyWebService(
    settings.Spotlight.host,
    settings.Spotlight.port,
    scala.concurrent.ExecutionContext.Implicits.global
  )
  val explorer = new FileExplorer(
    jobName = "evaluate-manual-annotations",
    forceProcess = true,
    extensions = List("annotation.json"),
    directory = Some(settings.Evaluation.Manual.folder),
    nbTasks = 1
  )

  // Define how files will be processed
  def process(file: File, ec: ExecutionContext): Future[Option[String]] = {
    // Parse it
    val json = parse(file)
    val task = json.extract[Annotation]

    // Index the resource
    val graph2 = new Graph2(ec, annotatorWs)

    graph2.index(task).map(solution => {

      solution.annotations.foreach {
        case AnnotationElement(uris, _, indexes) =>
          println()
          println(s"\turis: $uris")
          println(s"\tindexes: $indexes")
      }

      // Evaluate the solution
      val statistic = new Statistics(solution, 25)
      println("precision:")
      statistic.precision().foreach(println)
      println("recall:")
      statistic.recall().foreach(println)
      println("f1:")
      statistic.f1().foreach(println)

      Some("OK")
    })(ec)
  }

  explorer.launch(process)
  annotatorWs.shutdown
}
