package agents

import java.io.File

import agents.helpers.FileExplorer
import org.json4s.native.JsonMethods._
import rsc.attributes.Source
import rsc.prettifier.{PrettifierType, V1}
import rsc.writers.Json
import rsc.{Formatters, Resource}

import scala.concurrent.{ExecutionContext, Future}

object Prettifier extends App with Formatters {

  val explorer = new FileExplorer("prettifier", forceProcess = true)
  val prettifier = new V1()

  def process(file: File, ec: ExecutionContext) = {
    // Parse it
    val json = parse(file)
    val r = json.extract[Resource]

    val (forceBook, forceKeyword) = r.source match {
      case Source.Coursera |
           Source.Khan |
           Source.MIT |
           Source.Scholarpedia => (false, true)

      case Source.Safari => (true, false)
    }

    val newOTocs = r.oTocs.map {
      case tocs => tocs.map {
        case toc => prettifier.process(
          toc,
          forceKeywordMode = forceKeyword,
          forceBookMode = forceBook)
      }
    }

    val newR = r.copy(oTocs = newOTocs, oPrettifier = Some(PrettifierType.V1))
    Json.write(newR, Some(file.getAbsolutePath))

    Future.successful()
  }

  explorer.launch(process)
}
