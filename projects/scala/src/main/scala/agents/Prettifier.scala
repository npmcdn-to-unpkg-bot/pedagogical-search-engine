package agents

import java.io.File

import agents.helpers.FileExplorer
import rsc.Formatters
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}

object Prettifier extends App with Formatters {

  val explorer = new FileExplorer("prettifier")

  def process(file: File, ec: ExecutionContext) = {
    Future {
      Logger.info(s"I don't care about: '${file.getName}'")
      Thread.sleep((math.random * 1000).toInt)
    }(ec)
  }

  explorer.launch(process)
}
