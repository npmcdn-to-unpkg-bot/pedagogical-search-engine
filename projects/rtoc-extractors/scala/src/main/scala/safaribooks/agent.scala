package safaribooks

import java.io.File

import rsc.Worker
import safaribooks.Types.Book
import utils.Logger

object agent {
  def main(args: Array[String]): Unit = {
    // Load program arguments
    if(args.length != 2) {
      Logger.error(s"2 arguments expected: (a) input-folder (b) output-folder")
      System.exit(1)
    }
    val input = new File(args(0))
    val output = new File(args(1))

    // Check (input/output)-folder arg
    (input.isDirectory, output.isDirectory) match {
      case (true, true) => {
        // Locate the course folder
        val inputPath = input.getAbsolutePath
        val bookFolder  = new File(inputPath + "/sample-500")

        // Create the data file if necessary
        val dataPath = new File(s"$inputPath/data.json")
        if(!dataPath.exists()) {
          dataPath.createNewFile()
        }

        // Create the worker
        val factory = new Factory(output)
        val data = new DataFile(dataPath, bookFolder)
        val worker = new Worker[Book](data, factory)

        // Make the worker work
        worker.work()

        // Exit
        System.exit(0)
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
