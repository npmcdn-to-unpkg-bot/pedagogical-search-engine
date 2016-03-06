package coursera

import java.io.File

import utils.Logger
import coursera.Types.Course
import rtoc.Worker

object agent {
  def main(args: Array[String]): Unit = {
    // Load program arguments
    if(args.length != 2) {
      Logger.error(s"2 arguments expected: (a) input-folder (b) output-folder")
      System.exit(1)
    }
    val input = new File(args(0))
    val output = new File(args(1))

    // Check output-folder arg
    (input.isDirectory, output.isDirectory) match {
      case (true, true) => {
        val pre = input.getAbsolutePath
        val pages = new File(s"$pre/pages")
        val dataPath = new File(s"$pre/course-links.txt")

        // Check output-folder content
        (pages.isDirectory, dataPath.isFile) match {
          case (true, true) => {
            val factory = new Factory(pages, output)
            val data = new DataFile(dataPath, alreadyRunned = false)
            val worker = new Worker[Course](data, factory)

            // Make the worker work
            worker.work()

            // Exit
            System.exit(0)
          }
          case _ => {
            if(!pages.isDirectory) {
              val path = pages.getPath
              Logger.error(s"No folder found: $path")
            }
            if(!dataPath.isFile) {
              val path = dataPath.getPath
              Logger.error(s"No file found: $path")
            }
            System.exit(1)
          }
        }
      }
      case _ => {
        if(!input.isDirectory) {
          val path = input.getPath
          Logger.error(s"Input path should be a valid folder: $path")
        }
        if(!output.isDirectory) {
          val path = output.getPath
          Logger.error(s"Output path should be a valid folder: $path")
        }
        System.exit(1)
      }
    }
  }
}
