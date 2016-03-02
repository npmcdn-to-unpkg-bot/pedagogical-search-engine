package scholarpedia

import java.io.File

import Utils.Logger
import rtoc.Worker
import scholarpedia.Types.Downloaded


object agent {
  def main(args: Array[String]): Unit = {
    val outputFolder = new File(args(0))

    // Check output-folder arg
    outputFolder.isDirectory match {
      case false => {
        val path = outputFolder.getPath
        Logger.error(s"Output path should be a valid folder: $path")
        System.exit(1)
      }
      case true => {
        val pre = outputFolder.getAbsolutePath
        val pages = new File(s"$pre/pages")
        val screenshots = new File(s"$pre/screenshots")
        val dataPath = new File(s"$pre/data.json")

        // Check output-folder content
        (pages.isDirectory, screenshots.isDirectory, dataPath.isFile) match {
          case (true, true, true) => {
            // Create the worker
            val factory = new Factory(pages)
            val data = new Articles(dataPath)
            val worker = new Worker[Downloaded](data, factory)

            // Make the worker work
            worker.work()
          }
          case _ => {
            if(!pages.isDirectory) {
              val path = pages.getPath
              Logger.error(s"No 'pages' folder found: $path")
            }
            if(!screenshots.isDirectory) {
              val path = pages.getPath
              Logger.error(s"No 'screenshots' folder found: $path")
            }
            if(!dataPath.isFile) {
              val path = pages.getPath
              Logger.error(s"No 'data.json' file found: $path")
            }
            System.exit(1)
          }
        }
      }
    }
  }
}
