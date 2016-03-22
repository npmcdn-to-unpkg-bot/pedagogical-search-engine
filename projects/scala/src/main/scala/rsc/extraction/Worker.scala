package rsc.extraction

import rsc.writers.Writer
import utils.Logger

class Worker[U <: HasStatus](data: Data[U], factory: Factory[U], writer: Writer) {
  def work(): Unit = {
    // (re)-Initialize the cursor
    data.init()

    // Do all the work
    def workRec(): Unit = data.hasNext() match {
      case true => {
        val entry = data.next()
        factory.getResource(entry) match {
          case None => {
            data.markNotOk(entry)
            Logger.info(s"Skipped: $entry")
          }
          case Some(resource) => {
            writer.write(resource)
            data.markOk(entry)
            Logger.info(s"OK: $entry")
          }
        }

        // Continue the work
        workRec()
      }
      case false => {}
    }
    workRec()

    // Flush results
    data.flush()
  }

}
